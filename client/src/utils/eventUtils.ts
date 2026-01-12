import type { SectionResponse, SeatResponse } from '../generated/api';

export const calculateTotal = (
  sections: SectionResponse[],
  selectedSection: string | null,
  selectedSeatsCount: number
): number => {
  const section = sections.find(s => s.id === selectedSection);
  if (!section || !section.price) return 0;
  return Number(section.price) * selectedSeatsCount;
};

export const groupSeatsByRow = (seats: SeatResponse[]): Record<string, SeatResponse[]> => {
  return seats.reduce((acc, seat) => {
    const rowLabel = seat.rowLabel || '';
    if (!acc[rowLabel]) {
      acc[rowLabel] = [];
    }
    acc[rowLabel].push(seat);
    return acc;
  }, {} as Record<string, SeatResponse[]>);
};

