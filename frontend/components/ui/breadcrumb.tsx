import * as React from "react";
import { ChevronRight } from 'lucide-react';

interface BreadcrumbProps {
  children: React.ReactNode;
  className?: string;
}

export function Breadcrumb({ children, className = "", ...props }: BreadcrumbProps) {
  return (
    <nav 
      className={`flex items-center text-sm text-gray-400 ${className}`} 
      aria-label="Breadcrumb" 
      {...props}
    >
      <ol className="flex items-center space-x-2">
        {children}
      </ol>
    </nav>
  );
}

interface BreadcrumbItemProps {
  children: React.ReactNode;
  isCurrentPage?: boolean;
}

export function BreadcrumbItem({ children, isCurrentPage = false }: BreadcrumbItemProps) {
  return (
    <li className="flex items-center">
      <div className={`flex items-center ${isCurrentPage ? 'text-white' : ''}`}>
        {children}
      </div>
      {!isCurrentPage && <ChevronRight className="ml-2 h-4 w-4" />}
    </li>
  );
}

interface BreadcrumbLinkProps {
  children: React.ReactNode;
  href?: string;
  asChild?: boolean;
}

export function BreadcrumbLink({ children, href, asChild = false }: BreadcrumbLinkProps) {
  if (asChild) {
    return <>{children}</>;
  }

  if (!href) {
    return <span>{children}</span>;
  }

  return (
    <a href={href} className="hover:text-white transition-colors">
      {children}
    </a>
  );
}
