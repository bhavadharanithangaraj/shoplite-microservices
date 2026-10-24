"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import AppBar from "@mui/material/AppBar";
import Button from "@mui/material/Button";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import StorefrontIcon from "@mui/icons-material/Storefront";

const LINKS = [
  { href: "/products", label: "Products" },
  { href: "/orders", label: "Orders" },
];

export default function NavBar() {
  const pathname = usePathname();

  return (
    <AppBar position="static" elevation={0}>
      <Toolbar sx={{ gap: 1, flexWrap: "wrap" }}>
        <StorefrontIcon />
        <Typography variant="h6" sx={{ mr: 2, fontWeight: 700 }}>
          ShopLite
        </Typography>
        {LINKS.map((link) => (
          <Button
            key={link.href}
            component={Link}
            href={link.href}
            color="inherit"
            sx={{ fontWeight: 600, opacity: pathname.startsWith(link.href) ? 1 : 0.75 }}
          >
            {link.label}
          </Button>
        ))}
      </Toolbar>
    </AppBar>
  );
}
