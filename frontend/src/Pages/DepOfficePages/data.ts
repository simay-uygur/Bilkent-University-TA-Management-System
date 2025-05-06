// src/pages/AssignProctor/data.ts
import { Exam, TA } from './AssignProctorRow';

export const sampleExams: Exam[] = [
  {
    id: 'e1',
    courseName: 'Algorithms',
    courseId: 'CS225',
    level: 'BS',
    examType: 'Midterm',
    date: '2025-05-10',
    startTime: '09:00',
    endTime: '11:00',
    needed: 3,
    tasLeft: 0,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'Prefered' },
      { id: 'ta3', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'None'     },
      { id: 'ta2', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, wantedState: 'Unprefered' },
    ],
    potentialTAs: [
      { id: 'ta4', name: 'John Doe',      level: 'BS',  workload: 3, hasAdjacentExam: true,  wantedState: 'Prefered'   },
      { id: 'ta5', name: 'Jane Smith',    level: 'MS',  workload: 0, hasAdjacentExam: false, wantedState: 'None'       },
      { id: 'ta6', name: 'Emily Johnson', level: 'PhD', workload: 5, hasAdjacentExam: true,  wantedState: 'Unprefered' },
    ],
  },
  {
    id: 'e2',
    courseName: 'Data Structures',
    courseId: 'CS226',
    level: 'MS',
    examType: 'Final',
    date: '2025-06-01',
    startTime: '13:00',
    endTime: '15:00',
    needed: 4,
    tasLeft: 0,
    assignedTAs: [
      { id: 'ta1', name: 'Ali Veli',   level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'None'       },
      { id: 'ta2', name: 'Ayşe Fatma', level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'Prefered'   },
      { id: 'ta3', name: 'Mehmet Can', level: 'PhD', workload: 1, hasAdjacentExam: false, wantedState: 'Unprefered' },
      { id: 'ta4', name: 'John Doe',   level: 'BS',  workload: 3, hasAdjacentExam: true,  wantedState: 'None'       },
    ],
    potentialTAs: [
      { id: 'ta5', name: 'Jane Smith',    level: 'MS',  workload: 0, hasAdjacentExam: false, wantedState: 'Prefered'   },
      { id: 'ta6', name: 'Emily Johnson', level: 'PhD', workload: 5, hasAdjacentExam: true,  wantedState: 'None'       },
    ],
  },
  {
    id: 'e3',
    courseName: 'Machine Learning',
    courseId: 'CS450',
    level: 'PhD',
    examType: 'Midterm',
    date: '2025-05-20',
    startTime: '10:00',
    endTime: '12:00',
    needed: 2,
    tasLeft: 1,
    assignedTAs: [
      { id: 'ta2', name: 'Ayşe Fatma',    level: 'MS',  workload: 4, hasAdjacentExam: true,  wantedState: 'Unprefered' },
    ],
    potentialTAs: [
      { id: 'ta1', name: 'Ali Veli',      level: 'BS',  workload: 2, hasAdjacentExam: false, wantedState: 'Prefered'   },
      { id: 'ta5', name: 'Jane Smith',    level: 'MS',  workload: 0, hasAdjacentExam: false, wantedState: 'None'       },
      { id: 'ta6', name: 'Emily Johnson', level: 'PhD', workload: 5, hasAdjacentExam: true,  wantedState: 'Unprefered' },
    ],
  },
];
