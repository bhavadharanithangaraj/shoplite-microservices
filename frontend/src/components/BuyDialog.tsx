"use client";

import { useState, type FormEvent } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { ApiError, placeOrder } from "@/lib/api";
import type { Order, Product } from "@/lib/types";

interface Props {
  product: Product;
  onClose: () => void;
  onOrdered: (order: Order, product: Product) => void;
}

// Places an order through order-service. Any backend error
// (not enough stock, unknown product, inventory-service down) is shown as-is.
export default function BuyDialog({ product, onClose, onOrdered }: Props) {
  const [quantity, setQuantity] = useState("1");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const qty = Number(quantity);
    if (!Number.isInteger(qty) || qty < 1) {
      setError("Quantity must be a whole number of at least 1");
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const order = await placeOrder(product.id, qty);
      onOrdered(order, product);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not place the order");
      setSubmitting(false);
    }
  }

  return (
    <Dialog open onClose={() => !submitting && onClose()} fullWidth maxWidth="xs">
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>Buy {product.name}</DialogTitle>
        <DialogContent>
          <Typography color="text.secondary" sx={{ mb: 2 }}>
            {product.stock} in stock
          </Typography>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <TextField
            label="Quantity"
            type="number"
            value={quantity}
            onChange={(e) => {
              setQuantity(e.target.value);
              setError(null);
            }}
            slotProps={{ htmlInput: { min: 1 } }}
            autoFocus
            fullWidth
          />
          <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 1 }}>
            Tip: ask for more than is in stock to see the error from the backend.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? "Placing order..." : "Place order"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
