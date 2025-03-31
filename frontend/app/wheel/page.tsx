'use client';

import { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import ShellLayout from '../components/VSCodeLayout/ShellLayout';
import WheelComponent from '../components/LuckyDrawWheel/WheelComponent';
import { Button } from '@/components/ui/button';
import { Modal } from '@/components/ui/modal';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink } from '@/components/ui/breadcrumb';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { PartyPopper, Frown, Info, Medal, Gift, Percent, Package, RotateCw, Crown } from 'lucide-react';

// Types for our wheel segments and prizes
interface WheelSegment {
  id: number;
  text: string;
  color: string;
  probability?: number;
  isReward?: boolean;
  rewardValue?: string;
  // Removed image property to avoid 404 errors
}

interface Prize {
  id: number;
  name: string; 
  description: string;
  value: string;
  color: string;
  icon: any; // Using Lucide icons instead of images
}

// Mock prizes data using Lucide icons instead of images
const availablePrizes: Prize[] = [
  { 
    id: 1, 
    name: "Gift Card", 
    description: "A $50 gift card for your next purchase", 
    value: "$50", 
    color: "#e91e63",
    icon: Gift
  },
  { 
    id: 2, 
    name: "Free Product", 
    description: "One free product of your choice", 
    value: "$25", 
    color: "#9c27b0",
    icon: Package
  },
  { 
    id: 3, 
    name: "Discount", 
    description: "25% off your next purchase", 
    value: "25% OFF", 
    color: "#3f51b5",
    icon: Percent
  },
  { 
    id: 4, 
    name: "Bonus Spins", 
    description: "Get 3 additional spins", 
    value: "3 Spins", 
    color: "#2196f3",
    icon: RotateCw
  },
  { 
    id: 5, 
    name: "Premium Pass", 
    description: "Access to exclusive content for 30 days", 
    value: "30 Days", 
    color: "#009688",
    icon: Crown
  },
];

// Generate wheel segments based on available prizes
const generateWheelSegments = (prizes: Prize[]): WheelSegment[] => {
  const segments: WheelSegment[] = [];
  
  // Add prize segments - WITHOUT images
  prizes.forEach(prize => {
    segments.push({
      id: prize.id,
      text: prize.name,
      color: prize.color,
      isReward: true,
      rewardValue: prize.value,
      probability: 15, // Default probability for prizes
    });
  });
  
  // Add some "Try Again" segments
  const tryAgainCount = Math.max(3, Math.floor(segments.length * 0.6)); // At least 3 try again segments
  for (let i = 0; i < tryAgainCount; i++) {
    segments.push({
      id: 1000 + i,
      text: "Try Again",
      color: "#607d8b", // Gray color for "Try Again"
      isReward: false,
      probability: 25 // Higher probability for try again
    });
  };
  
  return segments;
};

export default function WheelPage() {
  const [isSpinning, setIsSpinning] = useState(false);
  const [segments, setSegments] = useState<WheelSegment[]>([]);
  const [currentEvent, setCurrentEvent] = useState<string>("Summer Giveaway");
  const [resultModal, setResultModal] = useState(false);
  const [result, setResult] = useState<WheelSegment | null>(null);
  const [remainingSpins, setRemainingSpins] = useState(3);
  const [spinHistory, setSpinHistory] = useState<Array<{result: WheelSegment, timestamp: string}>>([]);
  
  const searchParams = useSearchParams();
  const eventId = searchParams?.get('event') || '1';
  
  useEffect(() => {
    try {
      // Generate wheel segments
      const wheelSegments = generateWheelSegments(availablePrizes);
      console.log('WheelPage: Generated wheel segments:', wheelSegments);
      setSegments(wheelSegments);
      
      // In a real implementation, you would fetch event details based on eventId
      setCurrentEvent("Summer Giveaway");
      
      // For demo purposes, set initial spins
      setRemainingSpins(3);
    } catch (error) {
      console.error('Error in WheelPage useEffect:', error);
    }
  }, [eventId]);
  
  // Show loading state if segments aren't loaded yet
  if (!segments || segments.length === 0) {
    return (
      <ShellLayout>
        <div className="container mx-auto py-6 px-4">
          <div className="text-center p-8">
            <h2 className="text-xl font-bold mb-4">Loading wheel...</h2>
          </div>
        </div>
      </ShellLayout>
    );
  }
  
  const handleSpinFinished = (segment: WheelSegment) => {
    try {
      // Record this spin in history
      const now = new Date();
      setSpinHistory(prev => [{
        result: segment,
        timestamp: now.toLocaleTimeString()
      }, ...prev]);
      
      // Decrement remaining spins
      setRemainingSpins(prev => Math.max(0, prev - 1));
      
      // Show result modal
      setResult(segment);
      setResultModal(true);
    } catch (error) {
      console.error('Error in handleSpinFinished:', error);
    }
  };
  
  const closeResultModal = () => {
    setResultModal(false);
  };
  
  // Function to handle claiming a reward
  const claimReward = () => {
    alert("Reward claimed! Check your email for details.");
    closeResultModal();
  };
  
  return (
    <ShellLayout>  
      <main className="container mx-auto py-6 px-4">
        <Breadcrumb className="mb-6">
          <BreadcrumbItem>
            <BreadcrumbLink href="/">Home</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbItem>
            <BreadcrumbLink href="/events">Events</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbItem isCurrentPage>
            <BreadcrumbLink>Lucky Draw Wheel</BreadcrumbLink>
          </BreadcrumbItem>
        </Breadcrumb>
        
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">{currentEvent} - Lucky Draw</h1>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Left Column - Spins Info */}
          <div className="flex flex-col gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Your Spins</CardTitle>
                <CardDescription>Remaining spins and status</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex flex-col items-center py-4">
                  <div className="text-4xl font-bold text-[#007acc] mb-2">{remainingSpins}</div>
                  <div className="text-gray-400">Remaining Spins</div>
                  
                  <div className="mt-6 w-full">
                    <Button 
                      className="w-full" 
                      disabled={remainingSpins <= 0}
                      onClick={() => {
                        // Scroll to wheel if needed
                        document.getElementById('wheel-section')?.scrollIntoView({ behavior: 'smooth' });
                      }}
                    >
                      {remainingSpins > 0 ? "Spin Now" : "No Spins Left"}
                    </Button>
                  </div>
                  
                  <div className="mt-4 p-3 bg-[#2d2d2d] rounded-md w-full">
                    <div className="flex items-center">
                      <Info size={16} className="mr-2 text-gray-400" />
                      <p className="text-sm text-gray-400">
                        You can earn more spins by participating in other event activities.
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Rewards Overview</CardTitle>
                <CardDescription>Potential prizes you can win</CardDescription>
              </CardHeader>
              <CardContent className="max-h-[400px] overflow-y-auto">
                <div className="space-y-3">
                  {availablePrizes.map(prize => (
                    <div key={prize.id} className="p-3 bg-[#2d2d2d] rounded-md flex items-center">
                      <div className="w-8 h-8 rounded-full mr-3" style={{ backgroundColor: prize.color }}></div>
                      <div>
                        <div className="font-medium">{prize.name}</div>
                        <div className="text-sm text-gray-400">{prize.value}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
          
          {/* Middle Column - The Wheel */}
          <div className="flex flex-col items-center" id="wheel-section">
            <Card className="w-full">
              <CardHeader>
                <CardTitle className="text-center">Spin The Wheel</CardTitle>
                <CardDescription className="text-center">Try your luck today!</CardDescription>
              </CardHeader>
              <CardContent className="flex justify-center py-8">
                <WheelComponent 
                  segments={segments}
                  primaryColor="#007acc"
                  contrastColor="#ffffff"
                  buttonText={isSpinning ? "SPINNING..." : "SPIN"}
                  size={280}
                  upDuration={100}
                  downDuration={1000}
                  onFinished={handleSpinFinished}
                  spinning={isSpinning}
                  setSpinning={setIsSpinning}
                  isOnlyOnce={false}
                />
              </CardContent>
            </Card>
            
            <div className="mt-4 flex flex-col items-center justify-center p-4 bg-[#2d2d2d] rounded-md w-full">
              <Medal className="h-5 w-5 text-[#007acc] mb-2" />
              <p className="text-sm text-center text-gray-300">
                Golden Hours offer 2x the chances to win! Check the schedule and come back during these special times.
              </p>
            </div>
          </div>
          
          {/* Right Column - Spin History */}
          <div>
            <Card className="h-full">
              <CardHeader>
                <CardTitle>Spin History</CardTitle>
                <CardDescription>Your recent spins and results</CardDescription>
              </CardHeader>
              <CardContent>
                {spinHistory.length > 0 ? (
                  <div className="space-y-3 max-h-[600px] overflow-y-auto pr-2">
                    {spinHistory.map((spin, index) => (
                      <div 
                        key={index} 
                        className={`p-3 rounded-md flex items-center ${
                          spin.result.isReward ? 'bg-[#1e3a5f]' : 'bg-[#2d2d2d]'
                        }`}
                      >
                        <div 
                          className="w-8 h-8 rounded-full mr-3 flex items-center justify-center"
                          style={{ backgroundColor: spin.result.color }}
                        >
                          {spin.result.isReward ? "🎁" : "✓"}
                        </div>
                        <div className="flex-grow">
                          <div className="font-medium">
                            {spin.result.text}
                            {spin.result.rewardValue && ` - ${spin.result.rewardValue}`}
                          </div>
                          <div className="text-xs text-gray-400">
                            {spin.timestamp}
                          </div>
                        </div>
                        {spin.result.isReward && (
                          <Button variant="secondary" size="sm">
                            Claim
                          </Button>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center py-10 text-gray-400">
                    <Info size={48} className="mb-3 opacity-50" />
                    <p>No spins yet. Start spinning to see your history!</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
      
      {/* Result Modal */}
      <Modal 
        isOpen={resultModal} 
        onClose={closeResultModal}
        title={result?.isReward ? "Congratulations! 🎉" : "Better Luck Next Time"}
        className="max-w-md"
      >
        <div className="p-6 flex flex-col items-center text-center">
          {result?.isReward ? (
            <>
              <div className="w-24 h-24 rounded-full bg-[#1e3a5f] flex items-center justify-center mb-4">
                <PartyPopper size={48} className="text-[#007acc]" />
              </div>
              <h2 className="text-2xl font-bold mb-2">You Won!</h2>
              <p className="mb-4 text-gray-300">
                You've won a {result.text} {result.rewardValue && `(${result.rewardValue})`}!
              </p>
              <Button onClick={claimReward} className="mt-2 w-full">
                Claim Your Prize
              </Button>
            </>
          ) : (
            <>
              <div className="w-24 h-24 rounded-full bg-[#2d2d2d] flex items-center justify-center mb-4">
                <Frown size={48} className="text-gray-400" />
              </div>
              <h2 className="text-2xl font-bold mb-2">No Prize This Time</h2>
              <p className="mb-4 text-gray-300">
                Don't give up! You still have {remainingSpins} spins left.
              </p>
              <Button onClick={closeResultModal} className="mt-2 w-full">
                Try Again
              </Button>
            </>
          )}
          
          {remainingSpins === 0 && !result?.isReward && (
            <p className="mt-4 text-yellow-500">
              You've used all your spins. Come back tomorrow for more chances!
            </p>
          )}
        </div>
      </Modal>
    </ShellLayout>
  );
}
