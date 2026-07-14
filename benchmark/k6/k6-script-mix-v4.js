import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

export const options = {
  scenarios: {
    warmup: {
      executor: 'constant-vus',
      vus: 10,
      duration: '10s',
      gracefulStop: '0s',
      tags: { stage: 'warmup' },
    },
    load: {
      executor: 'constant-vus',
      vus: 500,
      duration: '30s', // Can be overridden in CLI
      startTime: '10s',
      gracefulStop: '5s',
      tags: { stage: 'load' },
    }
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'],
  },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8084';

export function setup() {
  const shortCodes = [];
  const payload = JSON.stringify({
    originalUrl: 'https://github.com/flourine95/url-shortener',
  });
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Pre-seed some URLs for reads
  for (let i = 0; i < 100; i++) {
    const res = http.post(`${BASE_URL}/api/urls`, payload, params);
    if (res.status === 201) {
      const body = JSON.parse(res.body);
      if (body && body.data && body.data.shortCode) {
        shortCodes.push(body.data.shortCode);
      }
    }
  }
  return { shortCodes };
}

export default function (data) {
  const rand = Math.random();
  if (rand < 0.10) {
    // 10% Write (POST)
    const uniqueId = Math.random().toString(36).substring(7);
    const payload = JSON.stringify({
      originalUrl: `https://github.com/flourine95/url-shortener/${uniqueId}`,
    });
    const params = {
      headers: {
        'Content-Type': 'application/json',
      },
    };
    const res = http.post(`${BASE_URL}/api/urls`, payload, params);
    check(res, {
      'POST status is 201': (r) => r.status === 201,
    });
  } else {
    // 90% Read (GET Redirect)
    const shortCodes = data.shortCodes;
    if (!shortCodes || shortCodes.length === 0) {
      fail('No short codes generated in setup');
    }
    const randomCode = shortCodes[Math.floor(Math.random() * shortCodes.length)];
    const params = {
      redirects: 0,
      headers: {
        'User-Agent': 'k6-load-test',
      }
    };
    const res = http.get(`${BASE_URL}/${randomCode}`, params);
    check(res, {
      'GET status is 302': (r) => r.status === 302,
      'GET has Location header': (r) => r.headers['Location'] !== undefined,
    });
  }

  sleep(0.01);
}

export function handleSummary(data) {
  return {
    "benchmark/reports/report-mix-v4.html": htmlReport(data),
  };
}
