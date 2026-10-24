"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Loading from "@/components/Loading";
import { ApiError, getOrders, getProducts } from "@/lib/api";
import type { Order } from "@/lib/types";

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [productNames, setProductNames] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Orders only store a productId (the product lives in the other service),
    // so we also ask inventory-service for names and join them here in the UI.
    getOrders()
      .then(setOrders)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load orders"));

    getProducts()
      .then((products) => setProductNames(Object.fromEntries(products.map((p) => [p.id, p.name]))))
      .catch(() => {
        // Not critical: we fall back to showing "Product #id"
      });
  }, []);

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 3 }}>
        Orders
      </Typography>

      {error && <Alert severity="error">{error}</Alert>}
      {!error && !orders && <Loading />}

      {orders && orders.length === 0 && (
        <Alert severity="info" action={<Button component={Link} href="/products">Go shopping</Button>}>
          No orders yet.
        </Alert>
      )}

      {orders && orders.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Order</TableCell>
                <TableCell>Product</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Placed at</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orders.map((order) => (
                <TableRow key={order.id} hover>
                  <TableCell sx={{ fontWeight: 600 }}>#{order.id}</TableCell>
                  <TableCell>{productNames[order.productId] ?? `Product #${order.productId}`}</TableCell>
                  <TableCell>{order.quantity}</TableCell>
                  <TableCell>
                    <Chip label={order.status} color="success" size="small" />
                  </TableCell>
                  <TableCell>{new Date(order.createdAt).toLocaleString()}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </>
  );
}
