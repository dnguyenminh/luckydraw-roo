import React from 'react';
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Sidebar from './components/VSCodeLayout/Sidebar';
import SidebarDetector from './components/Debug/SidebarDetector';

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
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
    <html lang="en" className={`${geistSans.variable} ${geistMono.variable}`}>
      <body className="bg-[#1e1e1e] text-white font-sans">
        {/* Debug component to detect duplicate sidebars */}
        <SidebarDetector />
        
        <div className="flex h-screen">
          {/* Main Sidebar Navigation with fixed base width */}
          <div id="main-sidebar" className="flex-shrink-0 bg-[#252526] border-r border-[#3c3c3c] overflow-hidden">
            <Sidebar />
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