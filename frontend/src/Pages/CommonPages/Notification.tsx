// src/pages/Not.tsx
import React, { useState } from 'react';
import LeaveReqMes, { LeaveReqMesProps } from '../../components/Messages/LeaveReqMes';
import NotificationMes, { NotificationMesProps } from '../../components/Messages/NotificationMes';
import styles from './Notification.module.css';

// Shared date formatter
const pad = (n: number) => n.toString().padStart(2, '0');
const formatDateTime = (date: Date, hour: number, minute: number) => {
  const d = pad(date.getDate()), m = pad(date.getMonth() + 1), y = date.getFullYear();
  return `${d}.${m}.${y} ${pad(hour)}:${pad(minute)}`;
};

// Sample workload for the leave component
const sampleWorkloads: NonNullable<LeaveReqMesProps['workloads']> = {
  proctorings: [{ date: new Date(2025,3,30), hour: 8, minute: 0, description: 'Physics 101 Proctoring' }],
  labs: [],
  recitations: [],
};

// Discriminated union for our six message types
type LeaveMsg = { id: string; type: 'leave'; payload: LeaveReqMesProps };
type ApproveWorkMsg = { id: string; type: 'approveWork'; payload: {
  taName: string;
  taskType: 'Lab'|'Recitation'|'Grading'|'Proctoring';
  course: string;
  courseId: string;
  date: Date;
  hour: number;
  minute: number;
  confirmText: string;
  variantClass?: string;
}};
type TransferMsg = { id: string; type: 'transfer'; payload: {
  taName: string;
  course: string;
  courseId: string;
  date: Date;
  hour: number;
  minute: number;
  confirmText: string;
  variantClass?: string;
}};
type SwapMsg = { id: string; type: 'swap'; payload: {
  taName: string;
  fromCourse: string;
  fromCourseId: string;
  fromDate: Date;
  fromHour: number;
  fromMinute: number;
  toCourse: string;
  toCourseId: string;
  toDate: Date;
  toHour: number;
  toMinute: number;
  confirmText: string;
  variantClass?: string;
}};
type OtherFacMsg = { id: string; type: 'otherFaculty'; payload: {
  departmentName: string;
  demandedTANum: number;
  course: string;
  courseId: string;
  date: Date;
  hour: number;
  minute: number;
  confirmText: string;
  variantClass?: string;
}};
type InFacMsg = { id: string; type: 'inFaculty'; payload: {
  departmentName: string;
  demandedTANum: number;
  course: string;
  courseId: string;
  date: Date;
  hour: number;
  minute: number;
  confirmText: string;
  variantClass?: string;
}};

type Message = LeaveMsg | ApproveWorkMsg | TransferMsg | SwapMsg | OtherFacMsg | InFacMsg;

export default function Notification() {
  const today = new Date(2025,3,30);

  const initial: Message[] = [
    {
      id: 'm1', type: 'leave',
      payload: {
        taName: 'Alice',
        startDate: new Date(2025,3,30,9,0),
        endDate:   new Date(2025,3,30,12,30),
        excuse:    'Alice reports a medical appointment on 30.04.2025 09:00 to 30.04.2025 12:30.',
        fileUrl:   '/files/alice_medical.pdf',
        workloads: sampleWorkloads,
      }
    },
    {
      id: 'm2', type: 'approveWork',
      payload: {
        taName: 'Grace',
        taskType: 'Lab',
        course: 'PHYS', courseId: 'PHYS201',
        date: today, hour: 14, minute: 0,
        confirmText: 'Approve completion of Grace?',
        variantClass: 'variant-green',
      }
    },
    {
      id: 'm3', type: 'transfer',
      payload: {
        taName: 'Charlie',
        course: 'Math 201', courseId: 'MTH201',
        date: today, hour: 10, minute: 0,
        confirmText: 'Confirm transfer request?',
        variantClass: 'variant-green',
      }
    },
    {
      id: 'm4', type: 'swap',
      payload: {
        taName: 'Eve',
        fromCourse: 'Physics', fromCourseId: 'PHY101',
        fromDate: today, fromHour: 8, fromMinute: 0,
        toCourse: 'Chemistry', toCourseId: 'CHM102',
        toDate: today, toHour: 12, toMinute: 0,
        confirmText: 'Confirm swap request?',
        variantClass: 'variant-green',
      }
    },
    {
      id: 'm5', type: 'otherFaculty',
      payload: {
        departmentName: 'Electrical Engineering',
        demandedTANum: 2,
        course: 'EE101', courseId: 'EE101',
        date: today, hour: 13, minute: 30,
        confirmText: 'Assign TA from other faculty?',
        variantClass: 'variant-green',
      }
    },
    {
      id: 'm6', type: 'inFaculty',
      payload: {
        departmentName: 'Computer Science',
        demandedTANum: 1,
        course: 'CS319', courseId: 'CS319',
        date: today, hour: 15, minute: 0,
        confirmText: 'Assign TA from own faculty?',
        variantClass: 'variant-green',
      }
    },
  ];

  const [messages, setMessages] = useState<Message[]>(initial);
  const [confirmId, setConfirmId] = useState<string|null>(null);

  function remove(id: string) {
    setMessages(ms => ms.filter(m => m.id !== id));
    if (confirmId === id) setConfirmId(null);
  }

  return (
    <div className={styles.container}>
      <main className={styles.messagesContainer}>
        {messages.map(msg => {
          if (msg.type === 'leave') {
            return (
              <LeaveReqMes
                key={msg.id}
                {...msg.payload}
                onApprove={() => remove(msg.id)}
                onReject={()  => remove(msg.id)}
              />
            );
          }

          // build label for generic notifications
          let label = '';
          if (msg.type === 'approveWork') {
            const p = msg.payload;
            label = `${p.taName} did the ${p.taskType} of the ${p.course}-${p.courseId} on ${formatDateTime(p.date, p.hour, p.minute)}.`;
          } else if (msg.type === 'transfer') {
            const p = msg.payload;
            label = `${p.taName} demands transfer for proctoring of ${p.course}-${p.courseId} on ${formatDateTime(p.date, p.hour, p.minute)}.`;
          } else if (msg.type === 'swap') {
            const p = msg.payload;
            label = `${p.taName} demands swap for proctoring of ${p.fromCourse}-${p.fromCourseId} at ${formatDateTime(p.fromDate, p.fromHour, p.fromMinute)} with ${p.toCourse}-${p.toCourseId} at ${formatDateTime(p.toDate, p.toHour, p.toMinute)}.`;
          } else if (msg.type === 'otherFaculty') {
            const p = msg.payload;
            label = `${p.departmentName} demands ${p.demandedTANum} TA(s) for the proctoring of ${p.course}-${p.courseId} at ${formatDateTime(p.date, p.hour, p.minute)} from other faculties.`;
          } else if (msg.type === 'inFaculty') {
            const p = msg.payload;
            label = `${p.departmentName} demands ${p.demandedTANum} TA(s) for the proctoring of ${p.course}-${p.courseId} at ${formatDateTime(p.date, p.hour, p.minute)} from in faculty.`;
          }

          // Now render the generic NotificationMes
          return (
            <NotificationMes
              key={msg.id}
              label={label}
              visible={true}
              onApproveClick={() => setConfirmId(msg.id)}
              onRejectClick={() => remove(msg.id)}
              confirmOpen={confirmId === msg.id}
              confirmText={msg.payload.confirmText}
              onConfirmApprove={() => remove(msg.id)}
              onCancelConfirm={() => setConfirmId(null)}
              variantClass={msg.payload.variantClass}
            />
          );
        })}
      </main>
    </div>
  );
}
