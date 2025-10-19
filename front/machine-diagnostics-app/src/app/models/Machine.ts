export interface Machine {
  id: string;
  name: string;
  temperature: number;
  vibration: number;
  currentPercentOfRated: number;
  overloadTripCount: number;
  status: string;
  context: string;
  recommendations: string[];
}