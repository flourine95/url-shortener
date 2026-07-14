
# How do you integrate with the URL shortener API?

This page documents the REST API endpoints of the URL shortener, describing request payloads, response schemas, and status codes.

## Overview

The application exposes REST endpoints using JavaScript Object Notation (JSON) payloads. You access these endpoints at the default base URL `http://localhost:8081` or through architectural profile ports.

Each API response returns a standardized JSON structure:

```json
{
  "data": { ... },
  "meta": null,
  "code": null,
  "message": "Action completed successfully",
  "success": true
}
```

If an operation fails, the `success` field is `false`, and the response provides error details in the `code` and `message` fields:

```json
{
  "data": null,
  "meta": null,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "success": false
}
```

## Endpoints

### Shorten a URL

Creates a short code redirection for a target destination.

```http
POST /api/urls
Content-Type: application/json
```

#### Request body

```json
{
  "originalUrl": "https://github.com/flourine95/url-shortener",
  "customCode": "your_custom_code_here",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

- **originalUrl**: String. Required. Must be a valid http or https URL.
- **customCode**: String. Optional. A unique alias for the redirect link.
- **expiresAt**: String. Optional. Future ISO 8601 UTC timestamp.

#### Response

`201 Created`

```json
{
  "data": {
    "id": 1234567890123,
    "originalUrl": "https://github.com/flourine95/url-shortener",
    "shortCode": "your_custom_code_here",
    "createdAt": "2026-07-14T04:39:11Z",
    "updatedAt": "2026-07-14T04:39:11Z",
    "expiresAt": "2026-12-31T23:59:59Z"
  },
  "meta": null,
  "code": null,
  "message": "URL shortened successfully",
  "success": true
}
```

---

### Redirect a short code

Redirects the client to the destination URL using an HTTP 302 status code.

```http
GET /{shortCode}
```

- **shortCode**: String. Required. The unique alias code path.

#### Response

`302 Found`

- Header **Location**: Contains the destination URL.

---

### List URLs

Retrieves a paginated list of shortened URLs, with optional filtering and sorting.

```http
GET /api/urls?q=wiki&status=active&sort=createdAt,desc&page=0&size=20
```

- **q**: String. Optional. Text search query to filter original URLs or short codes.
- **status**: String. Optional. Filter by status (`active` or `expired`).
- **sort**: String. Default is `createdAt,desc`. Sorting property and direction.
- **page**: Integer. Default is `0`. Zero-indexed page number.
- **size**: Integer. Default is `20`. Number of items per page.

#### Response

`200 OK`

```json
{
  "data": [
    {
      "shortCode": "your_short_code_here",
      "originalUrl": "https://example.com/wiki",
      "expiresAt": "2026-12-31T23:59:59Z",
      "createdAt": "2026-07-14T04:39:11Z",
      "totalClicks": 1234,
      "lastClickedAt": "2026-07-14T13:19:28Z"
    }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalItems": 1,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true,
    "numberOfElements": 1,
    "empty": false
  },
  "code": null,
  "message": "URLs fetched successfully",
  "success": true
}
```

---

### Fetch click statistics

Retrieves total clicks and recent visit logs for a short code.

```http
GET /api/urls/{shortCode}/stats
```

#### Response

`200 OK`

```json
{
  "data": {
    "shortCode": "your_short_code_here",
    "totalClicks": 1234,
    "lastClickedAt": "2026-07-14T13:19:28Z"
  },
  "meta": null,
  "code": null,
  "message": "Stats fetched successfully",
  "success": true
}
```

---

### Delete a short code

Deletes a short code and clears the cache state.

```http
DELETE /api/urls/{shortCode}
```

#### Response

`200 OK`

```json
{
  "data": null,
  "meta": null,
  "code": null,
  "message": "URL deleted successfully",
  "success": true
}
```
