export const WEEKDAY_LABELS = ['日', '一', '二', '三', '四', '五', '六'];

export interface CalendarDay {
  date: string;
  day: number;
  weekday: number;
  inCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
}

const DATE_KEY_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

function normalizeDateKey(dateKey?: string): string {
  return formatDateKey(parseDateKey(dateKey));
}

export function parseDateKey(dateKey?: string): Date {
  if (!dateKey || !DATE_KEY_PATTERN.test(dateKey)) {
    return new Date();
  }
  const [year, month, day] = dateKey.split('-').map(Number);
  const parsed = new Date(year, month - 1, day);
  if (
    parsed.getFullYear() !== year
    || parsed.getMonth() !== month - 1
    || parsed.getDate() !== day
  ) {
    return new Date();
  }
  return parsed;
}

export function formatDateKey(date: Date): string {
  const safeDate = Number.isNaN(date.getTime()) ? new Date() : date;
  const year = safeDate.getFullYear();
  const month = pad2(safeDate.getMonth() + 1);
  const day = pad2(safeDate.getDate());
  return `${year}-${month}-${day}`;
}

export function todayKey(): string {
  return formatDateKey(new Date());
}

export function startOfMonthKey(dateKey: string): string {
  const date = parseDateKey(dateKey);
  return formatDateKey(new Date(date.getFullYear(), date.getMonth(), 1));
}

export function endOfMonthKey(dateKey: string): string {
  const date = parseDateKey(dateKey);
  return formatDateKey(new Date(date.getFullYear(), date.getMonth() + 1, 0));
}

export function addMonthsKey(dateKey: string, offset: number): string {
  const date = parseDateKey(dateKey);
  return formatDateKey(new Date(date.getFullYear(), date.getMonth() + offset, 1));
}

export function addDaysKey(dateKey: string, offset: number): string {
  const date = parseDateKey(dateKey);
  date.setDate(date.getDate() + offset);
  return formatDateKey(date);
}

export function startOfWeekKey(dateKey: string): string {
  const date = parseDateKey(dateKey);
  date.setDate(date.getDate() - date.getDay());
  return formatDateKey(date);
}

export function endOfWeekKey(dateKey: string): string {
  return addDaysKey(startOfWeekKey(dateKey), 6);
}

export function calendarRangeForMonth(dateKey: string) {
  const monthStart = startOfMonthKey(dateKey);
  const monthEnd = endOfMonthKey(dateKey);
  return {
    start: startOfWeekKey(monthStart),
    end: endOfWeekKey(monthEnd),
  };
}

export function buildMonthDays(monthKey: string, selectedDate: string): CalendarDay[] {
  const range = calendarRangeForMonth(monthKey);
  const days: CalendarDay[] = [];
  const month = startOfMonthKey(monthKey).slice(0, 7);
  const safeSelectedDate = normalizeDateKey(selectedDate);
  for (let index = 0; index < 42; index += 1) {
    const cursor = addDaysKey(range.start, index);
    if (cursor > range.end && days.length >= 35) {
      break;
    }
    const date = parseDateKey(cursor);
    days.push({
      date: cursor,
      day: date.getDate(),
      weekday: date.getDay(),
      inCurrentMonth: cursor.slice(0, 7) === month,
      isToday: cursor === todayKey(),
      isSelected: cursor === safeSelectedDate,
    });
  }
  return days;
}

export function weekDaysFor(dateKey: string): CalendarDay[] {
  const safeDateKey = normalizeDateKey(dateKey);
  const start = startOfWeekKey(safeDateKey);
  const days: CalendarDay[] = [];
  for (let index = 0; index < 7; index += 1) {
    const date = addDaysKey(start, index);
    const parsed = parseDateKey(date);
    days.push({
      date,
      day: parsed.getDate(),
      weekday: parsed.getDay(),
      inCurrentMonth: date.slice(0, 7) === safeDateKey.slice(0, 7),
      isToday: date === todayKey(),
      isSelected: date === safeDateKey,
    });
  }
  return days;
}

export function formatMonthTitle(dateKey: string): string {
  const date = parseDateKey(dateKey);
  return `${date.getFullYear()}年${date.getMonth() + 1}月`;
}

export function formatDateTitle(dateKey: string): string {
  const date = parseDateKey(dateKey);
  return `${date.getMonth() + 1}月${date.getDate()}日 周${WEEKDAY_LABELS[date.getDay()]}`;
}

export function formatFullDateTitle(dateKey: string): string {
  const date = parseDateKey(dateKey);
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 周${WEEKDAY_LABELS[date.getDay()]}`;
}

export function weekNumberOfYear(dateKey: string): number {
  const date = parseDateKey(dateKey);
  const firstDay = new Date(date.getFullYear(), 0, 1);
  const dayOfYear = Math.floor((date.getTime() - firstDay.getTime()) / 86400000) + 1;
  return Math.ceil((dayOfYear + firstDay.getDay()) / 7);
}

export function formatTime(time?: string): string {
  return safeTime(time).slice(0, 5);
}

export function formatTimeRange(startTime?: string, endTime?: string): string {
  const start = formatTime(startTime);
  const end = formatTime(endTime);
  return start && end ? `${start}-${end}` : start || end || '';
}

export function timeToHour(time?: string): number {
  const value = safeTime(time);
  if (!value) return -1;
  const hour = Number(value.slice(0, 2));
  return Number.isNaN(hour) ? -1 : hour;
}

export function compareScheduleTime(a?: string, b?: string): number {
  return safeTime(a).localeCompare(safeTime(b));
}

export function pad2(value: number): string {
  return value < 10 ? `0${value}` : String(value);
}

function safeTime(time?: string): string {
  return typeof time === 'string' ? time : '';
}
