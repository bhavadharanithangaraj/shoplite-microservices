"use client";

import { useCallback, useEffect, useState } from "react";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Snackbar from "@mui/material/Snackbar";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import AddIcon from "@mui/icons-material/Add";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import AddProductDialog from "@/components/AddProductDialog";
import BuyDialog from "@/components/BuyDialog";
import Loading from "@/components/Loading";
import { ApiError, getProducts } from "@/lib/api";
import type { Order, Product } from "@/lib/types";

function StockChip({ stock }: { stock: number }) {
  if (stock === 0) return <Chip label="Out of stock" color="error" size="small" />;
  if (stock <= 5) return <Chip label={`${stock} left`} color="warning" size="small" />;
  return <Chip label={`${stock} in stock`} color="success" size="small" variant="outlined" />;
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [buying, setBuying] = useState<Product | null>(null);
  const [adding, setAdding] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const loadProducts = useCallback(() => {
    setError(null);
    getProducts()
      .then(setProducts)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load products"));
  }, []);

  useEffect(loadProducts, [loadProducts]);

  function handleOrdered(order: Order, product: Product) {
    setBuying(null);
    setToast(`Order #${order.id} confirmed: ${order.quantity} × ${product.name}`);
    loadProducts(); // stock changed in inventory-service, so fetch it again
  }

  function handleCreated(product: Product) {
    setAdding(false);
    setToast(`Added ${product.name}`);
    loadProducts();
  }

  const totalUnits = products?.reduce((sum, p) => sum + p.stock, 0) ?? 0;
  const lowStock = products?.filter((p) => p.stock <= 5).length ?? 0;

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2, gap: 2, flexWrap: "wrap" }}>
        <Typography variant="h4" fontWeight={700}>
          Products
        </Typography>
        <Button variant="outlined" startIcon={<AddIcon />} onClick={() => setAdding(true)}>
          Add product
        </Button>
      </Box>

      <Alert severity="info" sx={{ mb: 3 }}>
        Clicking <strong>Buy</strong> calls <strong>order-service</strong>, which checks and reduces the stock in{" "}
        <strong>inventory-service</strong> using OpenFeign, then saves the order in its own database.
      </Alert>

      {error && (
        <Alert severity="error" action={<Button onClick={loadProducts}>Retry</Button>} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {!error && !products && <Loading />}

      {products && (
        <>
          <Box sx={{ display: "grid", gap: 2, mb: 3, gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" } }}>
            <StatCard label="Products" value={products.length} />
            <StatCard label="Units in stock" value={totalUnits} />
            <StatCard label="Low or out of stock" value={lowStock} />
          </Box>

          {products.length === 0 ? (
            <Alert severity="info">No products yet. Click &quot;Add product&quot;.</Alert>
          ) : (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Product</TableCell>
                    <TableCell>Stock</TableCell>
                    <TableCell align="right">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {products.map((product) => (
                    <TableRow key={product.id} hover>
                      <TableCell>{product.id}</TableCell>
                      <TableCell sx={{ fontWeight: 600 }}>{product.name}</TableCell>
                      <TableCell>
                        <StockChip stock={product.stock} />
                      </TableCell>
                      <TableCell align="right">
                        <Button
                          variant="contained"
                          size="small"
                          startIcon={<ShoppingCartOutlinedIcon />}
                          onClick={() => setBuying(product)}
                          disabled={product.stock === 0}
                        >
                          Buy
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      )}

      {buying && <BuyDialog key={buying.id} product={buying} onClose={() => setBuying(null)} onOrdered={handleOrdered} />}
      {adding && <AddProductDialog onClose={() => setAdding(false)} onCreated={handleCreated} />}

      <Snackbar
        open={!!toast}
        autoHideDuration={4000}
        onClose={() => setToast(null)}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert severity="success" variant="filled" onClose={() => setToast(null)}>
          {toast}
        </Alert>
      </Snackbar>
    </>
  );
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <Card>
      <CardContent>
        <Typography variant="body2" color="text.secondary" fontWeight={600}>
          {label}
        </Typography>
        <Typography variant="h4" fontWeight={700} color="primary">
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
}
