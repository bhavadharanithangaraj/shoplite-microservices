"use client";

import { useState, type FormEvent } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import { ApiError, createProduct } from "@/lib/api";
import type { Product } from "@/lib/types";

interface Props {
  onClose: () => void;
  onCreated: (product: Product) => void;
}

export default function AddProductDialog({ onClose, onCreated }: Props) {
  const [name, setName] = useState("");
  const [stock, setStock] = useState("10");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const stockNumber = Number(stock);
    // Same rules as ProductRequest.java
    if (!name.trim()) return setError("Name is required");
    if (name.length > 100) return setError("Name must be at most 100 characters");
    if (!Number.isInteger(stockNumber) || stockNumber < 0) return setError("Stock must be a whole number, 0 or more");

    setSubmitting(true);
    setError(null);
    try {
      onCreated(await createProduct(name.trim(), stockNumber));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create the product");
      setSubmitting(false);
    }
  }

  return (
    <Dialog open onClose={() => !submitting && onClose()} fullWidth maxWidth="xs">
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>Add product</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} autoFocus fullWidth />
            <TextField
              label="Stock"
              type="number"
              value={stock}
              onChange={(e) => setStock(e.target.value)}
              slotProps={{ htmlInput: { min: 0 } }}
              fullWidth
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? "Saving..." : "Add"}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
