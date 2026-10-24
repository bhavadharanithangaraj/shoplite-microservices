// The browser talks to BOTH microservices directly:
//   inventory-service -> products and stock
//   order-service     -> orders (which then calls inventory-service itself, via OpenFeign)

import type { Order, Product } from "./types";

const ORDER_API = process.env.NEXT_PUBLIC_ORDER_API_URL ?? "http://localhost:8081";
const INVENTORY_API = process.env.NEXT_PUBLIC_INVENTORY_API_URL ?? "http://localhost:8082";

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(serviceName: string, url: string, options: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(url, {
      ...options,
      headers: { "Content-Type": "application/json", ...options.headers },
      cache: "no-store",
    });
  } catch {
    throw new ApiError(0, `Cannot reach ${serviceName}. Is it running?`);
  }

  if (!response.ok) {
    // Both services send { status, error, message } on errors (see ErrorResponse.java)
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.message ?? `${serviceName} returned ${response.status}`);
  }
  return (await response.json()) as T;
}

// ---- inventory-service ----

export function getProducts() {
  return request<Product[]>("inventory-service", `${INVENTORY_API}/products`);
}

export function createProduct(name: string, stock: number) {
  return request<Product>("inventory-service", `${INVENTORY_API}/products`, {
    method: "POST",
    body: JSON.stringify({ name, stock }),
  });
}

// ---- order-service ----

export function getOrders() {
  return request<Order[]>("order-service", `${ORDER_API}/orders`);
}

export function placeOrder(productId: number, quantity: number) {
  return request<Order>("order-service", `${ORDER_API}/orders`, {
    method: "POST",
    body: JSON.stringify({ productId, quantity }),
  });
}
