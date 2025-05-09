import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectCourseTA.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

interface PreferTasRequest {
  requestId: number;
  sectionCode: string;
  courseCode: string;
  taNeeded: number;
  preferredTas: Array<{id: number; name: string; surname: string}>;
  nonPreferredTas: Array<{id: number; name: string; surname: string}>;
}

const SelectCourseTA: React.FC = () => {
  const navigate = useNavigate();
  // Now using requestId as the URL parameter
  const { requestId } = useParams<{ requestId: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [requestData, setRequestData] = useState<PreferTasRequest | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: '',
    courseName: '',
    needed: 3, // Default value
    assignedTAs: [],
    potentialTAs: [],
  });
  
  // Parse from section code: e.g. "CS-464-1-2025-FALL" → "CS-464 Section 1 (FALL 2025)"
  function formatSectionName(code: string): string {
    const parts = code.split('-');
    if (parts.length >= 5) {
      return `${parts[0]}-${parts[1]} Section ${parts[2]} (${parts[4]} ${parts[3]})`;
    }
    return code;
  }
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        if (!requestId) {
          setError('Missing request ID');
          setLoading(false);
          return;
        }
        
        // 1. First get the request data to get sectionCode and preferences
        const requestResponse = await axios.get(`/api/request/preferTas/${requestId}`);
        const requestData = requestResponse.data;
        setRequestData(requestData);
        
        const sectionCode = requestData.sectionCode;
        if (!sectionCode) {
          setError('Request does not contain a valid section code');
          setLoading(false);
          return;
        }
        
        // 2. Get department code from section code (CS-464-1-2025-FALL → CS)
        const departmentCode = sectionCode.split('-')[0];
        if (!departmentCode) {
          setError('Invalid section code format');
          setLoading(false);
          return;
        }
        
        // 3. Get all TAs from this department
        const tasResponse = await axios.get(`/api/ta/department/${departmentCode}`);
        const allDepartmentTAs = tasResponse.data;
        
        // 4. Create lookup sets for preferred/unpreferred TA IDs
        const preferredTaIds = new Set(
          requestData.preferredTas?.map((ta: any) => ta.id.toString()) || []
        );
        
        const unpreferredTaIds = new Set(
          requestData.nonPreferredTas?.map((ta: any) => ta.id.toString()) || []
        );
        
        console.log("Preferred TAs:", [...preferredTaIds]);
        console.log("Unpreferred TAs:", [...unpreferredTaIds]);
        
        // 5. Map all department TAs and mark their preference status
        const processedTAs = allDepartmentTAs.map((ta: any) => {
          const taIdString = ta.id.toString();
          
          // Check if this TA is in either preference list
          let preference = 'normal';
          if (preferredTaIds.has(taIdString)) {
            preference = 'preferred';
            console.log(`TA ${ta.name} ${ta.surname} (${taIdString}) is preferred`);
          } else if (unpreferredTaIds.has(taIdString)) {
            preference = 'unpreferred';
            console.log(`TA ${ta.name} ${ta.surname} (${taIdString}) is unpreferred`);
          }
          
          return {
            id: taIdString,
            name: `${ta.name} ${ta.surname}`,
            level: ta.academicLevel === 'PHD' ? 'PhD' : 
                  ta.academicLevel === 'MSC' ? 'MS' : 'BS',
            workload: ta.totalWorkload || 0,
            hasAdjacentExam: false,
            preference: preference as 'preferred' | 'unpreferred' | 'normal'
          };
        });
        
        // 6. Update course data with preferences and needed count
        setCourseData({
          id: sectionCode,
          courseName: formatSectionName(sectionCode),
          needed: requestData.taNeeded || 3,
          assignedTAs: [],
          potentialTAs: processedTAs
        });
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load data. Please try again.');
        setLoading(false);
      }
    };
    
    fetchData();
  }, [requestId]);

  // Create state for assigned TAs
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potentials when course data changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const needed = courseData.needed;
  const leftCount = Math.max(0, needed - assigned.length);
  
  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);
  
  // Functions to assign/unassign TAs
  const assignTA = (ta: TA) => {
    setAssigned(prev => [...prev, ta]);
    setPotential(prev => prev.filter(p => p.id !== ta.id));
  };
  
  const unassignTA = (ta: TA) => {
    setAssigned(prev => prev.filter(p => p.id !== ta.id));
    setPotential(prev => [...prev, ta]);
  };
  
  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      unassignTA(ta);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };
  
  const handleUnprefConfirm = () => {
    if (selectedUnpref) {
      assignTA(selectedUnpref);
      setSelectedUnpref(null);
    }
    setShowUnprefConfirm(false);
  };
  
  // Sort TAs by preference first, then workload
  const sortedPotential = useMemo(() => {
    return [...potential].sort((a, b) => {
      // First by preference
      const prefOrder = { 'preferred': 0, 'normal': 1, 'unpreferred': 2 };
      const prefDiff = prefOrder[a.preference] - prefOrder[b.preference];
      
      if (prefDiff !== 0) return prefDiff;
      
      // Then by workload
      return a.workload - b.workload;
    });
  }, [potential]);
  
  if (loading) {
    return <LoadingPage />;
  }
  
  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  const sectionCode = requestData?.sectionCode || '';

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/ta-requests" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
          <span>Request ID: {requestId}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
            <th>Preference</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const prefClass = 
              ta.preference === 'preferred'   ? styles.preferredRow :
              ta.preference === 'unpreferred' ? styles.unpreferredRow : '';
            
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${prefClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
                <td>
                  {ta.preference === 'preferred' ? '⭐ Preferred' :
                   ta.preference === 'unpreferred' ? '⚠️ Unpreferred' : 'Normal'}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs ({assigned.length}/{needed})</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'   ? styles.preferredItem :
              ta.preference === 'unpreferred' ? styles.unpreferredItem :
                                              styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} {ta.preference === 'preferred' ? '⭐' : 
                         ta.preference === 'unpreferred' ? '⚠️' : ''} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={() => setConfirmAssign(true)}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred by the instructor. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message={`Confirm assignment of ${assigned.length} TAs to ${courseData.courseName}?`}
          onConfirm={async () => {
            try {
              // 1. Approve the request first
              await axios.put(`/api/request/${requestId}/approve`);
              
              // 2. Send assignments to backend
              for (const ta of assigned) {
                await axios.post(`/api/sections/${sectionCode}/tas/${ta.id}`);
              }
              
              navigate('/department-office/ta-requests');
            } catch (err) {
              console.error('Error assigning TAs:', err);
              alert('Failed to assign TAs. Please try again.');
            } finally {
              setConfirmAssign(false);
            }
          }}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectCourseTA;
/* import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectCourseTA.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const SelectCourseTA: React.FC = () => {
  const navigate = useNavigate();
  const { sectionCode } = useParams<{ sectionCode: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: sectionCode || '',
    courseName: sectionCode || '',
    needed: 3, // Default value
    assignedTAs: [],
    potentialTAs: [],
  });
  
  // Parse from section code: e.g. "CS-464-1-2025-FALL" → "CS-464 Section 1 (FALL 2025)"
  function formatSectionName(code: string): string {
    const parts = code.split('-');
    if (parts.length >= 5) {
      return `${parts[0]}-${parts[1]} Section ${parts[2]} (${parts[4]} ${parts[3]})`;
    }
    return code;
  }
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        if (!sectionCode) {
          setError('Missing section code');
          setLoading(false);
          return;
        }
        
        // 1. Get department code from section code (CS-464-1-2025-FALL → CS)
        const departmentCode = sectionCode.split('-')[0];
        if (!departmentCode) {
          setError('Invalid section code format');
          setLoading(false);
          return;
        }
        
        // 2. Get all TAs from this department
        const tasResponse = await axios.get(`/api/ta/department/${departmentCode}`);
        const allDepartmentTAs = tasResponse.data;
        
        // 3. Get the request specific to this section to find preferences
        let requestData = null;
        try {
          // First get all requests for the department
          const allRequestsResponse = await axios.get(`/api/department/${departmentCode}/preferTas`);
          
          // Find the one matching our section
          const matchingRequest = allRequestsResponse.data.find(
            (req: any) => req.sectionCode === sectionCode
          );
          
          if (matchingRequest) {
            // Get detailed data for this specific request
            const requestResponse = await axios.get(`/api/request/preferTas/${matchingRequest.requestId}`);
            requestData = requestResponse.data;
            console.log("Found matching request:", requestData);
          }
        } catch (err) {
          console.warn("No preference request found for section:", sectionCode);
        }
        
        // 4. Create lookup sets for preferred/unpreferred TA IDs
        const preferredTaIds = new Set(
          requestData?.preferredTas?.map((ta: any) => ta.id.toString()) || []
        );
        
        const unpreferredTaIds = new Set(
          requestData?.nonPreferredTas?.map((ta: any) => ta.id.toString()) || []
        );
        
        console.log("Preferred TAs:", [...preferredTaIds]);
        console.log("Unpreferred TAs:", [...unpreferredTaIds]);
        
        // 5. Map all department TAs and mark their preference status
        const processedTAs = allDepartmentTAs.map((ta: any) => {
          const taIdString = ta.id.toString();
          
          // Check if this TA is in either preference list
          let preference = 'normal';
          if (preferredTaIds.has(taIdString)) {
            preference = 'preferred';
            console.log(`TA ${ta.name} ${ta.surname} (${taIdString}) is preferred`);
          } else if (unpreferredTaIds.has(taIdString)) {
            preference = 'unpreferred';
            console.log(`TA ${ta.name} ${ta.surname} (${taIdString}) is unpreferred`);
          }
          
          return {
            id: taIdString,
            name: `${ta.name} ${ta.surname}`,
            level: ta.academicLevel === 'PHD' ? 'PhD' : 
                  ta.academicLevel === 'MSC' ? 'MS' : 'BS',
            workload: ta.totalWorkload || 0,
            hasAdjacentExam: false,
            preference: preference as 'preferred' | 'unpreferred' | 'normal'
          };
        });
        
        // 6. Update course data with preferences and needed count
        setCourseData({
          id: sectionCode,
          courseName: formatSectionName(sectionCode),
          needed: requestData?.taNeeded || 3, // Use requested count or default
          assignedTAs: [],
          potentialTAs: processedTAs
        });
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load data. Please try again.');
        setLoading(false);
      }
    };
    
    fetchData();
  }, [sectionCode]);

  // Create state for assigned TAs
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potentials when course data changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const needed = courseData.needed;
  const leftCount = Math.max(0, needed - assigned.length);
  
  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);
  
  // Functions to assign/unassign TAs
  const assignTA = (ta: TA) => {
    setAssigned(prev => [...prev, ta]);
    setPotential(prev => prev.filter(p => p.id !== ta.id));
  };
  
  const unassignTA = (ta: TA) => {
    setAssigned(prev => prev.filter(p => p.id !== ta.id));
    setPotential(prev => [...prev, ta]);
  };
  
  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      unassignTA(ta);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };
  
  const handleUnprefConfirm = () => {
    if (selectedUnpref) {
      assignTA(selectedUnpref);
      setSelectedUnpref(null);
    }
    setShowUnprefConfirm(false);
  };
  
  // Sort TAs by preference first, then workload
  const sortedPotential = useMemo(() => {
    return [...potential].sort((a, b) => {
      // First by preference
      const prefOrder = { 'preferred': 0, 'normal': 1, 'unpreferred': 2 };
      const prefDiff = prefOrder[a.preference] - prefOrder[b.preference];
      
      if (prefDiff !== 0) return prefDiff;
      
      // Then by workload
      return a.workload - b.workload;
    });
  }, [potential]);
  
  if (loading) {
    return <LoadingPage />;
  }
  
  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
            <th>Preference</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const prefClass = 
              ta.preference === 'preferred'   ? styles.preferredRow :
              ta.preference === 'unpreferred' ? styles.unpreferredRow : '';
            
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${prefClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
                <td>
                  {ta.preference === 'preferred' ? '⭐ Preferred' :
                   ta.preference === 'unpreferred' ? '⚠️ Unpreferred' : 'Normal'}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs ({assigned.length}/{needed})</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'   ? styles.preferredItem :
              ta.preference === 'unpreferred' ? styles.unpreferredItem :
                                              styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} {ta.preference === 'preferred' ? '⭐' : 
                         ta.preference === 'unpreferred' ? '⚠️' : ''} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={() => setConfirmAssign(true)}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred by the instructor. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message={`Confirm assignment of ${assigned.length} TAs to ${courseData.courseName}?`}
          onConfirm={async () => {
            try {
              // Send assignments to backend
              for (const ta of assigned) {
                await axios.post(`/api/sections/${sectionCode}/tas/${ta.id}`);
              }
              navigate('/department-office/assign-course');
            } catch (err) {
              console.error('Error assigning TAs:', err);
              alert('Failed to assign TAs. Please try again.');
            } finally {
              setConfirmAssign(false);
            }
          }}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectCourseTA; */
/* import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectCourseTA.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

interface PreferenceRequest {
  requestId: number;
  sectionCode: string;
  taNeeded: number;
  preferredTas: Array<{id: number; name: string; surname: string}>;
  nonPreferredTas: Array<{id: number; name: string; surname: string}>;
}

const SelectCourseTA: React.FC = () => {
  const navigate = useNavigate();
  const { sectionCode } = useParams<{ sectionCode: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: sectionCode || '',
    courseName: sectionCode ? formatSectionName(sectionCode) : '',
    needed: 3, // Default value
    assignedTAs: [],
    potentialTAs: [],
  });

  // Helper function to format section name for display
  function formatSectionName(code: string): string {
    const parts = code.split('-');
    if (parts.length >= 5) {
      return `${parts[0]}-${parts[1]} Section ${parts[2]} (${parts[4]} ${parts[3]})`;
    }
    return code;
  }
  
  // Fetch TAs and preference data
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Extract department code from section code
        const departmentCode = sectionCode?.split('-')[0] || 'CS';
        
        // 1. First get all TAs for this department
        const tasResponse = await axios.get(`/api/ta/department/${departmentCode}`);
        const allTAs = tasResponse.data;
        
        // 2. Get preference requests for this department
        const prefRequestsResponse = await axios.get(`/api/department/${departmentCode}/preferTas`);
        
        // 3. Find the request matching our section code
        let preferenceData = null;
        const matchingRequest = prefRequestsResponse.data.find(
          (req: any) => req.sectionCode === sectionCode
        );
        
        if (matchingRequest) {
          // 4. Get detailed preference data
          const detailResponse = await axios.get(`/api/request/preferTas/${matchingRequest.requestId}`);
          preferenceData = detailResponse.data;
        }
        
        // Create sets of preferred and unpreferred TA IDs for quick lookup
        const preferredIds = new Set(
          preferenceData?.preferredTas?.map((ta: any) => ta.name()) || []
        );
        
        const unpreferredIds = new Set(
          preferenceData?.nonPreferredTas?.map((ta: any) => ta.name()) || []
        );
        
        // Transform all TAs with preference information
        const mappedTAs = allTAs.map((ta: any) => {
          // Determine preference status based on the sets
          let preference = 'normal';
          if (preferredIds.has(ta.name)) {
            preference = 'preferred';
          } else if (unpreferredIds.has(ta.name)) {
            preference = 'unpreferred';
          }
          
          return {
            id: ta.id.toString(),
            name: `${ta.name} ${ta.surname}`,
            level: ta.academicLevel === 'PHD' ? 'PhD' : 
                  ta.academicLevel === 'MSC' ? 'MS' : 'BS',
            workload: ta.totalWorkload || 0,
            hasAdjacentExam: false, // Default value
            preference: preference as 'preferred' | 'unpreferred' | 'normal'
          };
        });
        
        // Update course data
        setCourseData({
          id: sectionCode || '',
          courseName: sectionCode ? formatSectionName(sectionCode) : '',
          needed: preferenceData?.taNeeded || 3, // Use requested number if available
          assignedTAs: [],
          potentialTAs: mappedTAs,
        });
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load data. Please try again.');
        setLoading(false);
      }
    };
    
    fetchData();
  }, [sectionCode]);

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potential TAs when courseData changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  
  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  // Sort TAs by preference first (preferred, normal, unpreferred)
  // and then by workload
  const sortedPotential = useMemo(() => {
    return [...potential].sort((a, b) => {
      // First sort by preference
      const prefOrder = { 'preferred': 0, 'normal': 1, 'unpreferred': 2 };
      const prefDiff = prefOrder[a.preference] - prefOrder[b.preference];
      
      if (prefDiff !== 0) return prefDiff;
      
      // Then sort by workload
      return a.workload - b.workload;
    });
  }, [potential]);

  const handleConfirm = () => setConfirmAssign(true);
  
  const doConfirm = async () => { 
    try {
      // Send assignments to backend
      for (const ta of assigned) {
        await axios.post(`/api/sections/${sectionCode}/tas/${ta.id}`);
        console.log(`Assigned TA ${ta.id} to section ${sectionCode}`);
      }
      setConfirmAssign(false);
      navigate('/department-office/assign-course');
    } catch (err) {
      console.error('Error assigning TAs:', err);
      alert('Failed to assign TAs. Please try again.');
      setConfirmAssign(false);
    }
  };
  
  if (loading) {
    return <LoadingPage/>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
            <th>Preference</th>
          </tr>
        </thead>
        <tbody>
  {sortedPotential.map(ta => {
    const isAssigned = assigned.some(a => a.id === ta.id);
    const assignClass = isAssigned ? styles.assignedRow : '';
    const prefClass =
      ta.preference === 'preferred'   ? styles.preferredRow :
      ta.preference === 'unpreferred' ? styles.unpreferredRow :
                                        '';

    return (
      <tr
        key={ta.id}
        onClick={() => toggleSelect(ta)}
        className={`${styles.rowBase} ${assignClass} ${prefClass}`}
      >
        <td>{ta.name}</td>
        <td>{ta.level}</td>
        <td>{ta.workload}</td>
        <td>{ta.preference}</td>
      </tr>
    );
  })}
</tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} {ta.preference === 'preferred' ? '⭐' : 
                          ta.preference === 'unpreferred' ? '⚠️' : ''} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={handleConfirm}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred by the instructor. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectCourseTA; */
/* 
import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectCourseTA.module.css';
import axios from 'axios';
import LoadingPage from '../CommonPages/LoadingPage';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const SelectCourseTA: React.FC = () => {
  const navigate = useNavigate();
  // Change from courseId to sectionCode
  const { sectionCode } = useParams<{ sectionCode: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: sectionCode || 'c2',
    // Format the section code to look better in the UI
    courseName: sectionCode ? formatSectionName(sectionCode) : 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [],
  });

  // Helper function to format section name for display
  function formatSectionName(code: string): string {
    const parts = code.split('-');
    if (parts.length >= 5) {
      return `${parts[0]}-${parts[1]} Section ${parts[2]} (${parts[4]} ${parts[3]})`;
    }
    return code;
  }
  
  // Fetch TAs from API
  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        // Use relative URL to avoid CORS issues
        const response = await axios.get('/api/ta/department/CS');
        
        // Transform API response to match our component's expected format
        const fetchedTAs = response.data.map((ta: any) => ({
          id: ta.id.toString(),
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel === 'PHD' ? 'PhD' : 
                 ta.academicLevel === 'MSC' ? 'MS' : 'BS',
          workload: ta.totalWorkload || 0,
          hasAdjacentExam: false, // Default value
          preference: 'normal' // Default value
        }));
        
        setCourseData(prev => ({
          ...prev,
          potentialTAs: fetchedTAs
        }));
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching TAs:', err);
        setError('Failed to load TAs. Please try again.');
        setLoading(false);
      }
    };
    
    fetchTAs();
  }, [sectionCode]);

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potential TAs when courseData changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  
  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  
  const doConfirm = async () => { 
    try {
      // Update the API endpoint to match the backend structure
      for (const ta of assigned) {
        await axios.post(`/api/sections/${sectionCode}/tas/${ta.id}`);
        console.log(`Assigned TA ${ta.id} to section ${sectionCode}`);
      }
      setConfirmAssign(false);
      navigate('/department-office/assign-course');
    } catch (err) {
      console.error('Error assigning TAs:', err);
      alert('Failed to assign TAs. Please try again.');
      setConfirmAssign(false);
    }
  };
  
  if (loading) {
    return <LoadingPage/>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
  {sortedPotential.map(ta => {
    const isAssigned = assigned.some(a => a.id === ta.id);
    const assignClass = isAssigned ? styles.assignedRow : '';
    const prefClass =
      ta.preference === 'preferred'   ? styles.preferredRow :
      ta.preference === 'unpreferred' ? styles.unpreferredRow :
                                        '';
    return (
      <tr
        key={ta.id}
        onClick={() => toggleSelect(ta)}
        className={`${styles.rowBase} ${assignClass} ${prefClass}`}
      >
        <td>{ta.name}</td>
        <td>{ta.level}</td>
        <td>{ta.workload}</td>
      </tr>
    );
  })}
</tbody>
…
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={handleConfirm}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectCourseTA;  */
/* // src/pages/AssignTA/SelectTACourse.tsx
import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import DepOfNavBar from '../../components/NavBars/DepOfNavBar';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectTACourse.module.css';
import axios from 'axios';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const SelectTACourse: React.FC = () => {
  const navigate = useNavigate();
  const { courseId } = useParams<{ courseId: string }>();
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [courseData, setCourseData] = useState<Course>({
    id: courseId || 'c2',
    courseName: courseId ? `Course ${courseId}` : 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [],
  });
  
  // Fetch TAs from API
  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        const response = await axios.get('http://localhost:5173/api/ta/department/CS');
        
        // Transform API response to match our component's expected format
        const fetchedTAs = response.data.map((ta: any) => ({
          id: ta.id.toString(),
          name: `${ta.name} ${ta.surname}`,
          level: ta.academicLevel === 'PHD' ? 'PhD' : 
                 ta.academicLevel === 'MSC' ? 'MS' : 'BS',
          workload: ta.totalWorkload || 0,
          hasAdjacentExam: false, // Default value
          preference: 'normal' // Default value
        }));
        
        setCourseData(prev => ({
          ...prev,
          potentialTAs: fetchedTAs
        }));
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching TAs:', err);
        setError('Failed to load TAs. Please try again.');
        setLoading(false);
      }
    };
    
    fetchTAs();
  }, [courseId]);

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([]);
  const [potential, setPotential] = useState<TA[]>([]);
  
  // Update potential TAs when courseData changes
  useEffect(() => {
    setPotential(courseData.potentialTAs);
  }, [courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  
  // Rest of your component functionality remains the same
  
  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  
  const doConfirm = async () => { 
    try {
      // Save assignments to backend
      for (const ta of assigned) {
        await axios.post(`http://localhost:5173/api/sections/${courseId}/tas/${ta.id}`);
      }
      setConfirmAssign(false);
      navigate('/department-office/assign-course');
    } catch (err) {
      console.error('Error assigning TAs:', err);
      alert('Failed to assign TAs. Please try again.' + err);
      setConfirmAssign(false);
    }
  };
  
  if (loading) {
    return <div className={styles.loading}>Loading TAs...</div>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

     // {Rest of your component rendering remains the same }
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const isAssigned = assigned.some(a => a.id === ta.id);
            const rowClass = isAssigned
              ? styles.assignedRow
              : ta.preference === 'preferred'
              ? styles.preferredRow
              : ta.preference === 'unpreferred'
              ? styles.unpreferredRow
              : '';
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${rowClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button 
          className={styles.confirmButton} 
          onClick={handleConfirm}
          disabled={assigned.length === 0}
        >
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectTACourse; */
/* // src/pages/AssignTA/SelectTACourse.tsx
import React, { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SelectTACourse.module.css';

export interface TA {
  id: string;
  name: string;
  level: 'BS' | 'MS' | 'PhD';
  workload: number;
  hasAdjacentExam: boolean;
  preference: 'preferred' | 'unpreferred' | 'normal';
}

interface Course {
  id: string;
  courseName: string;
  needed: number;
  assignedTAs: TA[];
  potentialTAs: TA[];
  tasLeft?: number;
}

const sampleCourses: Course[] = [
  {
    id: 'c2',
    courseName: 'CS202 – Data Structures',
    needed: 3,
    assignedTAs: [],
    potentialTAs: [
      { id: 't4', name: 'David Kim', level: 'MS', workload: 4, hasAdjacentExam: false, preference: 'preferred' },
      { id: 't5', name: 'Eva Martinez', level: 'BS', workload: 2, hasAdjacentExam: false, preference: 'unpreferred' },
      { id: 't6', name: 'Frank Zhou', level: 'PhD', workload: 6, hasAdjacentExam: true, preference: 'normal' },
    ],
  },
];

const SelectTACourse: React.FC = () => {
  const navigate = useNavigate();
  const { courseId } = useParams<{ courseId: string }>();
  const courseData = sampleCourses.find(c => c.id === courseId) || sampleCourses[0];

  const needed = courseData.needed;
  const [assigned, setAssigned] = useState<TA[]>([...courseData.assignedTAs]);
  const [potential, setPotential] = useState<TA[]>([...courseData.potentialTAs]);

  const [confirmAssign, setConfirmAssign] = useState(false);
  const [showUnprefConfirm, setShowUnprefConfirm] = useState(false);
  const [selectedUnpref, setSelectedUnpref] = useState<TA | null>(null);

  const leftCount = Math.max(0, needed - assigned.length);
  courseData.tasLeft = leftCount;

  const assignTA = (ta: TA) => {
    const newAssigned = [...assigned, ta];
    setAssigned(newAssigned);
    setPotential(p => p.filter(a => a.id !== ta.id));
  };

  const toggleSelect = (ta: TA) => {
    if (assigned.some(a => a.id === ta.id)) {
      setAssigned(a => a.filter(x => x.id !== ta.id));
      setPotential(p => [...p, ta]);
    } else {
      if (ta.preference === 'unpreferred') {
        setSelectedUnpref(ta);
        setShowUnprefConfirm(true);
      } else if (assigned.length < needed) {
        assignTA(ta);
      }
    }
  };

  const handleUnprefConfirm = () => {
    if (selectedUnpref) assignTA(selectedUnpref);
    setShowUnprefConfirm(false);
  };

  const sortedPotential = useMemo(
    () => [...potential].sort((a, b) => a.workload - b.workload),
    [potential]
  );

  const handleConfirm = () => setConfirmAssign(true);
  const doConfirm = () => { setConfirmAssign(false); navigate('/department-office/assign-course/${courseID}/${courseSec}'); };

  return (
    <div className={styles.pageWrapper}>
<<<<<<<<< Temporary merge branch 1
      
=========
>>>>>>>>> Temporary merge branch 2

      <header className={styles.header}>
        <BackBut to="/department-office/assign-course" />
        <h1 className={styles.title}>{courseData.courseName}</h1>
        <div className={styles.stats}>
          <span>Needed: {needed}</span>
          <span>Left: {leftCount}</span>
        </div>
      </header>

      <table className={styles.table}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Level</th>
            <th>Workload</th>
          </tr>
        </thead>
        <tbody>
          {sortedPotential.map(ta => {
            const isAssigned = assigned.some(a => a.id === ta.id);
            const rowClass = isAssigned
              ? styles.assignedRow
              : ta.preference === 'preferred'
              ? styles.preferredRow
              : ta.preference === 'unpreferred'
              ? styles.unpreferredRow
              : '';
            return (
              <tr
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={`${styles.rowBase} ${rowClass}`}
              >
                <td>{ta.name}</td>
                <td>{ta.level}</td>
                <td>{ta.workload}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <section className={styles.selectedSection}>
        <h2>Assigned TAs</h2>
        <ul className={styles.selectedList}>
          {assigned.map(ta => {
            const itemClass =
              ta.preference === 'preferred'
                ? styles.preferredItem
                : ta.preference === 'unpreferred'
                ? styles.unpreferredItem
                : styles.normalItem;
            return (
              <li
                key={ta.id}
                onClick={() => toggleSelect(ta)}
                className={itemClass}
              >
                {ta.name} ×
              </li>
            );
          })}
        </ul>
      </section>

      <div className={styles.actions}>
        <button className={styles.confirmButton} onClick={handleConfirm}>
          Confirm Assignment
        </button>
      </div>

      {showUnprefConfirm && selectedUnpref && (
        <ConPop
          message="This TA is unpreferred. Are you sure you want to assign them?"
          onConfirm={handleUnprefConfirm}
          onCancel={() => setShowUnprefConfirm(false)}
        />
      )}

      {confirmAssign && (
        <ConPop
          message="Are you sure you want to finalize this assignment?"
          onConfirm={doConfirm}
          onCancel={() => setConfirmAssign(false)}
        />
      )}
    </div>
  );
};

export default SelectTACourse;
 */