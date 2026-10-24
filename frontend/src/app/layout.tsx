import type { Metadata } from "next";
import type { ReactNode } from "react";
import { AppRouterCacheProvider } from "@mui/material-nextjs/v15-appRouter";
import Container from "@mui/material/Container";
import NavBar from "@/components/NavBar";
import Providers from "@/components/Providers";

export const metadata: Metadata = {
  title: "ShopLite",
  description: "Order & Inventory microservices demo",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>
        {/* Makes MUI's CSS-in-JS styles render correctly with the App Router */}
        <AppRouterCacheProvider>
          <Providers>
            <NavBar />
            <Container maxWidth="lg" sx={{ py: 4 }}>
              {children}
            </Container>
          </Providers>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
