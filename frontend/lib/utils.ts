import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Combines tailwind classes with conditional logic
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
