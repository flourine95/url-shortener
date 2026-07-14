import http from 'k6/http';
import { check, sleep, fail } from 'k6';

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
      vus: 50,
      duration: '30s',
      startTime: '10s',
      gracefulStop: '5s',
      tags: { stage: 'load' },
    }
  },
  thresholds: {
    http_req_duration: ['p(95)<200'],
  },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8081';

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
    'status is 302': (r) => r.status === 302,
    'has Location header': (r) => r.headers['Location'] !== undefined,
  });

  sleep(0.01);
}
