// TypeScript versions of the JSON the two services return

export interface Product {
  id: number;
  name: string;
  stock: number;
}

export interface Order {
  id: number;
  productId: number;
  quantity: number;
  status: "CONFIRMED";
  createdAt: string;
}
