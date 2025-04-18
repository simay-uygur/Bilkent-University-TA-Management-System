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
export interface ScheduleItem {
  id: string;
  timeRange: string;
  task: string;
  lesson: string;
}

// example typed fetch; replace `any` with your real schedule interface
export function fetchSchedule(): Promise<AxiosResponse<any>> {
  return axios.get<ScheduleItem>('/api/ta/schedule', {
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
  
 /*  export function fetchSchedule() {
    return axios.get<ScheduleItem[]>('/api/ta/schedule', { withCredentials:true });
  } */
  
  export function fetchAvailableTAs() {
    return axios.get<{id:string;name:string}[]>('/api/ta/available', { withCredentials:true });
  }
  // fetch a single schedule item:
export function fetchScheduleItem(id: string) {
    return axios.get<ScheduleItem>(`/api/ta/schedule/${id}`, { withCredentials: true });
  }
  
  // payload for leave request:
  export interface LeaveRequestPayload {
    scheduleId: string;
    startTime: string;
    endTime: string;
    excuse: string;
    message: string;
  }
  
  export function submitLeaveRequest(payload: LeaveRequestPayload, file?: File) {
    const form = new FormData();
    form.append('startTime', payload.startTime);
    form.append('endTime', payload.endTime);
    form.append('excuse', payload.excuse);
    form.append('message', payload.message);
    if (file) form.append('attachment', file);
  
    return axios.post(
      `/api/ta/schedule/${payload.scheduleId}/leave-request`,
      form,
      { withCredentials: true, headers: { 'Content-Type': 'multipart/form-data' } }
    );
  }
  /** --- Volunteer Proctoring API --- **/

/**
 * Represents a single volunteer‑proctoring request.
 */
export interface VolunteerRequest {
  id: string;
  course: string;
  needed: number;       // number of TAs needed
  closesAt: string;     // e.g. '2025-03-06'
  priority?: boolean;   // if true, show the ★
  assigned: boolean;    // true if this TA is already assigned
}

/**
 * Fetches the list of volunteer‑proctoring requests for the given page.
 * @param page 1‑based page number
 */
export function fetchVolunteerRequests(
  page: number
): Promise<AxiosResponse<VolunteerRequest[]>> {
  return axios.get<VolunteerRequest[]>(
    `/api/volunteer-requests?page=${page}`,
    { withCredentials: true }
  );
}

/**
 * Toggles or sets the assignment for a particular volunteer request.
 * @param id the VolunteerRequest.id to assign/unassign
 */
export function assignVolunteer(
  id: string
): Promise<AxiosResponse<void>> {
  return axios.post<void>(
    `/api/volunteer-requests/${id}/assign`,
    {},
    { withCredentials: true }
  );
}
export interface Notification {
  id: string;
  source: string;
  message: string;
  timestamp: string; // ISO
  read: boolean;
}

export function fetchNotifications(): Promise<AxiosResponse<Notification[]>> {
  return axios.get<Notification[]>('/api/notifications', { withCredentials: true });
}

export function markAllNotificationsRead(): Promise<AxiosResponse<void>> {
  return axios.post<void>('/api/notifications/mark-read-all', {}, { withCredentials: true });
}