import { AdmissionCategory, ProgramType, FeeDetails } from '../types';
import { BUS_ROUTES_WITH_STOPS, HOSTEL_FEE_PER_YEAR } from './constants';

export const getCourseDurationYears = (program: ProgramType | undefined): number => {
  if (!program) return 0;
  switch (program) {
    case 'First Year B.Tech':
      return 4;
    case 'Second Year B.Tech (Lateral Entry)':
      return 3;
    case 'PG':
      return 2;
    default:
      return 0;
  }
};

export const getBaseTuitionFeePerYear = (
  category: AdmissionCategory | undefined,
  program: ProgramType | undefined
): number => {
  if (!program || !category) return 0;
  if (category === 'CENTAC') {
    return 75000;
  }
  // Management Category Fee Structure
  switch (program) {
    case 'First Year B.Tech':
      return 125000;
    case 'Second Year B.Tech (Lateral Entry)':
      return 115000;
    case 'PG':
      return 95000;
    default:
      return 0;
  }
};

export const calculateFeeDetails = (
  category: AdmissionCategory | undefined,
  program: ProgramType | undefined,
  cutOffMark: number | undefined,
  busTransportRequired: boolean,
  busRouteSelected: string,
  busStopSelected: string,
  hostelRequired: boolean
): FeeDetails => {
  const duration = getCourseDurationYears(program);
  const tuitionPerYear = getBaseTuitionFeePerYear(category, program);
  const totalTuitionFee = tuitionPerYear * duration;

  // Calculate bus fee from route and stop
  let busFee = 0;
  if (busTransportRequired && busRouteSelected && busStopSelected) {
    const routeStops = BUS_ROUTES_WITH_STOPS[busRouteSelected] || [];
    const stopMatch = routeStops.find((s) => s.stopName === busStopSelected);
    if (stopMatch) {
      busFee = stopMatch.fee;
    }
  }

  const hostelFee = hostelRequired ? HOSTEL_FEE_PER_YEAR : 0;
  const grandTotalFee = totalTuitionFee + busFee + hostelFee;

  return {
    cutOffMark: cutOffMark || 0,
    tuitionFeePerYear: tuitionPerYear,
    courseDurationYears: duration,
    totalTuitionFee,
    busTransportRequired,
    busRouteSelected,
    busStopSelected,
    busFee,
    hostelRequired,
    hostelFee,
    grandTotalFee,
  };
};
