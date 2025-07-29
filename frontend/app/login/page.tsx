'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Loader2, LogIn } from 'lucide-react';

function LoginPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams() || new URLSearchParams(); // Provide default value
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  // Get redirect destination if any
  const redirectTo = searchParams.get('redirectTo') || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Prevent submission if already loading
    if (isLoading) return;
    
    setError('');
    setIsLoading(true);
    
    try {
      // Here you would normally call your API endpoint for authentication
      // This is a mock implementation
      await new Promise(resolve => setTimeout(resolve, 1500)); // Simulate API call
      
      if (username === 'admin' && password === 'password') {
        // Store authentication token or user info
        localStorage.setItem('token', 'mock-token-12345');
        router.push(redirectTo); // Redirect to the requested page or home
      } else {
        setError('Invalid username or password');
      }
    } catch (err) {
      setError('An error occurred. Please try again.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#1e1e1e] p-4">
      <Card className="w-full max-w-md bg-[#252526] border-[#3c3c3c]">
        <CardHeader className="space-y-1">
          <div className="flex justify-center mb-4">
            <div className="w-10 h-10 bg-[#007acc] rounded-md flex items-center justify-center">
              <span className="text-white font-bold text-xl">LD</span>
            </div>
          </div>
          <CardTitle className="text-2xl text-center">Login</CardTitle>
          <CardDescription className="text-center">
            Enter your credentials to access the LuckyDraw system
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-red-900/20 border border-red-800 text-red-100 px-4 py-2 rounded text-sm">
                {error}
              </div>
            )}
            
            <div className="space-y-2">
              <label htmlFor="username" className="text-sm font-medium">
                Username
              </label>
              <Input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="bg-[#3c3c3c] border-[#555555] focus:ring-[#007acc] focus:border-[#007acc]"
                required
              />
            </div>
            
            <div className="space-y-2">
              <label htmlFor="password" className="text-sm font-medium">
                Password
              </label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="bg-[#3c3c3c] border-[#555555] focus:ring-[#007acc] focus:border-[#007acc]"
                required
              />
            </div>

            <div className="flex justify-end">
              <a href="#" className="text-sm text-[#007acc] hover:underline">
                Forgot password?
              </a>
            </div>
            
            <Button 
              type="submit" 
              className={`w-full bg-[#007acc] hover:bg-[#0069ac] ${isLoading ? 'opacity-70 cursor-not-allowed' : ''}`}
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" /> 
                  Logging in...
                </>
              ) : (
                <>
                  <LogIn className="mr-2 h-4 w-4" />
                  Login
                </>
              )}
            </Button>
            
            {/* Add loading overlay when processing */}
            {isLoading && (
              <div className="fixed inset-0 bg-black/20 flex items-center justify-center z-50" aria-hidden="true">
                <div className="bg-[#252526] p-4 rounded-md flex items-center gap-3">
                  <Loader2 className="h-5 w-5 animate-spin text-[#007acc]" />
                  <span>Processing...</span>
                </div>
              </div>
            )}
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-[#1e1e1e] p-4">
        <div className="flex items-center gap-3">
          <Loader2 className="h-6 w-6 animate-spin text-[#007acc]" />
          <span className="text-white">Loading...</span>
        </div>
      </div>
    }>
      <LoginPageContent />
    </Suspense>
  );
}
