import axios, { AxiosResponse } from 'axios';
axios.defaults.baseURL = import.meta.env.VITE_API_URL || '';  // ← point to your API

export interface Credentials {
  username: string;
  password: string;
}

// login returns nothing on success (assumes your Spring endpoint returns 200 OK with no body)
export function login(
  creds: Credentials
): Promise<AxiosResponse<void>> {
  return axios.post<void>('/api/auth/login', creds, {
    withCredentials: true,
  });
}

// example typed fetch; replace `any` with your real schedule interface
export function fetchSchedule(): Promise<AxiosResponse<any>> {
  return axios.get<any>('/api/ta/schedule', {
    withCredentials: true,
  });
}
export interface LeaveRequest {
    id: string;
    taName: string;
    course: string;
    proctoringDate: string;   // ISO date
    requestedTime: string;
    excuse: string;
    message: string;
    attachmentUrl?: string;
  }
  
  export function fetchLeaveRequests() {
    return axios.get<LeaveRequest[]>('/api/leave-requests/pending', { withCredentials:true });
  }
  export function approveLeaveRequest(id: string) {
    return axios.post(`/api/leave-requests/${id}/approve`, {}, { withCredentials:true });
  }
  export function rejectLeaveRequest(id: string) {
    return axios.post(`/api/leave-requests/${id}/reject`, {}, { withCredentials:true });
  }
  export interface Course {
    id: string;
    code: string;
    title: string;
    instructor: string;
    minTAs: number;
    maxTAs: number;
    mustPreferred: string[];
    avoidPreferred: string[];
    availableTAs: { id: string; name: string }[];
  }
  
  export function fetchCourses() {
    return axios.get<Course[]>('/api/courses', { withCredentials:true });
  }
  export function assignTA(courseId: string, taId: string) {
    return axios.post(`/api/courses/${courseId}/assign`, { taId }, { withCredentials:true });
  }
  export interface ScheduleItem {
    id: string;
    timeRange: string; // e.g. "08:00 - 10:00"
    task: 'Proctoring' | 'Leave Request';
  }
  
 /*  export function fetchSchedule() {
    return axios.get<ScheduleItem[]>('/api/ta/schedule', { withCredentials:true });
  } */
  
  export function fetchAvailableTAs() {
    return axios.get<{id:string;name:string}[]>('/api/ta/available', { withCredentials:true });
  }
  