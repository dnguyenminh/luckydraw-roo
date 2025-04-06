import React from 'react';
import type { Metadata } from "next";
import { Inter, JetBrains_Mono } from "next/font/google";
import "./globals.css";
import Sidebar from './components/VSCodeLayout/Sidebar';
import SidebarDetector from './components/Debug/SidebarDetector';

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

const jetbrainsMono = JetBrains_Mono({
  variable: "--font-jetbrains",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: 'Lucky Draw Management',
  description: 'Lucky Draw Management System',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${inter.variable} ${jetbrainsMono.variable}`}>
      <body className="bg-[#1e1e1e] text-white font-sans">
        {/* Debug component to detect duplicate sidebars */}
        {typeof SidebarDetector === 'function' ? <SidebarDetector /> : null}
        
        <div className="flex h-screen">
          {/* Main Sidebar Navigation with fixed base width */}
          <div id="main-sidebar" className="flex-shrink-0 bg-[#252526] border-r border-[#3c3c3c] overflow-hidden">
            {typeof Sidebar === 'function' ? <Sidebar /> : null}
          </div>
          
          {/* Main Content */}
          <div className="flex-1 overflow-y-auto">
            {children}
          </div>
        </div>
      </body>
    </html>
  );
}