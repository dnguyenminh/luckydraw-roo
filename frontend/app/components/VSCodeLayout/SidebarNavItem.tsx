'use client';

import Link from 'next/link';
import { ReactNode } from 'react';

interface SidebarNavItemProps {
  href: string;
  icon: ReactNode;
  label: string;
  isActive: boolean;
  collapsed: boolean;
}

export default function SidebarNavItem({
  href, 
  icon,
  label,
  isActive,
  collapsed
}: SidebarNavItemProps) {
  return (
    <li>
      <Link 
        href={href}
        className={`
          flex items-center p-2 rounded-md transition-colors
          ${isActive 
            ? 'bg-[#37373d] text-white' 
            : 'text-gray-400 hover:bg-[#2a2d2e] hover:text-white'
          }
          ${collapsed ? 'justify-center' : 'justify-start'}
        `}
      >
        <span className="w-5 h-5 flex items-center justify-center">
          {icon}
        </span>
        {!collapsed && <span className="ml-3 whitespace-nowrap">{label}</span>}
      </Link>
    </li>
  );
}
