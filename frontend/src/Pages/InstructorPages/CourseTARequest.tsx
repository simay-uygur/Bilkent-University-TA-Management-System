// src/pages/ExamProctor/CourseTAReq.tsx
import React, { useState, useEffect, FocusEvent } from 'react';
import { useLocation } from 'react-router-dom';

import BackBut from '../../components/Buttons/BackBut';

import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './CourseTARequest.module.css';
import axios from 'axios';
import SearchSelect from '../../components/SearchSelect';

// Updated TA interface to match API response
export interface TA {
  id: number;
  name: string;
  surname: string;
  academicLevel: string;
  totalWorkload: number;
  department: string;
  taType: string;
}

interface RawRequest {
  courseId: string;
  neededTAs: number;
  wantedTAs: number[];  // Changed to number[] since IDs from API are numbers
  unwantedTAs: number[]; // Changed to number[]
}

const CourseTAReq: React.FC = () => {
  const location = useLocation();
  
  // Extract course code from URL pattern: ".../assign-course/CS-464-1-2025-SPRING"
  const pathParts = location.pathname.split('/');
  const fullSectionCode = pathParts[pathParts.length - 1]; // Get the last part of the URL
  
  // Extract just the course code (CS-464) from the full section code
  const parts = fullSectionCode.split('-');
  const courseCode = parts.length >= 2 ? `${parts[0]}-${parts[1]}` : fullSectionCode;
  
  // Get section number for display
  const sectionNumber = parts.length >= 3 ? parts[2] : '1';
  
  // Rest of your component remains the same...
  
  // Add states for loading and API data
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tas, setTAs] = useState<TA[]>([]);

  // Fetch TAs from API
  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/ta/department/CS');
        setTAs(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching TAs:', err);
        setError('Failed to load TAs. Please try again.');
        setLoading(false);
      }
    };
    
    fetchTAs();
  }, []);

  const initialState = { needed: 0, wanted: [] as number[], unwanted: [] as number[] };
  const [state, setState] = useState(initialState);
  const [resetKey, setResetKey] = useState({ wanted: 0, unwanted: 0 });
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmData, setConfirmData] = useState<RawRequest | null>(null);

  const handleNeededFocus = (e: FocusEvent<HTMLInputElement>) => e.target.select();
  const handleNeededChange = (v: number) => setState(prev => ({ ...prev, needed: v }));

  const handleSelect = (field: 'wanted' | 'unwanted', taId: number) => {
    setState(prev => ({
      needed: prev.needed,
      wanted:
        field === 'wanted'
          ? Array.from(new Set([...prev.wanted, taId]))
          : prev.wanted.filter(id => id !== taId),
      unwanted:
        field === 'unwanted'
          ? Array.from(new Set([...prev.unwanted, taId]))
          : prev.unwanted.filter(id => id !== taId),
    }));
    setResetKey(prev => ({ ...prev, [field]: prev[field] + 1 }));
  };

  const handleDeselect = (field: 'wanted' | 'unwanted', taId: number) =>
    setState(prev => ({
      needed: prev.needed,
      wanted:
        field === 'wanted'
          ? prev.wanted.filter(id => id !== taId)
          : prev.wanted,
      unwanted:
        field === 'unwanted'
          ? prev.unwanted.filter(id => id !== taId)
          : prev.unwanted,
    }));

  const handleSubmit = () => {
    if (state.needed === 0) {
      setErrorMsg("You can't request 0 TAs.");
    } else {
      setConfirmData({
        courseId: courseCode,
        neededTAs: state.needed,
        wantedTAs: state.wanted,
        unwantedTAs: state.unwanted,
      });
    }
  };

  const handleConfirm = async () => {
    if (!confirmData) return;
    
    try {
      // POST payload to backend
      await axios.post('/api/ta-requests', confirmData);
      
      // reset all inputs on approval
      setConfirmData(null);
      setState(initialState);
      setResetKey({ wanted: 0, unwanted: 0 });
    } catch (err) {
      console.error('Error submitting TA request:', err);
      setErrorMsg('Failed to submit TA request. Please try again.');
    }
  };

  const selectedSet = new Set([...state.wanted, ...state.unwanted]);

  // Show loading state
  if (loading) {
    return <div className={styles.loading}>Loading TAs...</div>;
  }

  // Show error state
  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.headerRow}>
        <BackBut to="/instructor" />
        <h1 className={styles.title}>Course TA Request</h1>
      </div>

      <div className={styles.card}>
        <div className={styles.cardHeader}>{courseCode + "-" + sectionNumber}</div>

        <label className={styles.label}>
          TA Needed:
          <input
            type="number"
            min={0}
            value={state.needed}
            onFocus={handleNeededFocus}
            onChange={e => handleNeededChange(+e.target.value)}
            className={styles.inputNumber}
          />
        </label>

        <div className={styles.controlsRow}>
          <div className={styles.selectorRow}>
            <div className={styles.selectorBlock}>
              <span className={styles.selectorTitle}>Wanted TAs:</span>
              <SearchSelect<TA>
                key={`wanted-${resetKey.wanted}`}
                options={tas.filter(t => !selectedSet.has(t.id))}
                filterOption={t => `${t.name} ${t.surname} (${t.academicLevel})`}
                renderOption={t => <>{t.name} {t.surname} ({t.academicLevel})</>}
                placeholder="Pick a TA…"
                onSelect={t => handleSelect('wanted', t.id)}
                className={styles.searchSelect}
              />
              <div className={styles.selectedList}>
                {state.wanted.map(id => {
                  const ta = tas.find(t => t.id === id)!;
                  return (
                    <span key={id} className={styles.tag}>
                      {ta.name} {ta.surname} ({ta.academicLevel})
                      <button
                        type="button"
                        className={styles.tagRemove}
                        onClick={() => handleDeselect('wanted', id)}
                      >
                        ×
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>

            <div className={styles.selectorBlock}>
              <span className={styles.selectorTitle}>Unwanted TAs:</span>
              <SearchSelect<TA>
                key={`unwanted-${resetKey.unwanted}`}
                options={tas.filter(t => !selectedSet.has(t.id))}
                filterOption={t => `${t.name} ${t.surname} (${t.academicLevel})`}
                renderOption={t => <>{t.name} {t.surname} ({t.academicLevel})</>}
                placeholder="Pick a TA…"
                onSelect={t => handleSelect('unwanted', t.id)}
                className={styles.searchSelect}
              />
              <div className={styles.selectedList}>
                {state.unwanted.map(id => {
                  const ta = tas.find(t => t.id === id)!;
                  return (
                    <span key={id} className={styles.tag}>
                      {ta.name} {ta.surname} ({ta.academicLevel})
                      <button
                        type="button"
                        className={styles.tagRemove}
                        onClick={() => handleDeselect('unwanted', id)}
                      >
                        ×
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>
          </div>

          <div className={styles.cardSubmitWrapper}>
            <GreenBut text="Submit Request" onClick={handleSubmit} />
          </div>
        </div>
      </div>

      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}

      {confirmData && (
        <ConPop
          message="Are you sure you want to submit this request?"
          onConfirm={handleConfirm}
          onCancel={() => setConfirmData(null)}
        />
      )}
    </div>
  );
};

export default CourseTAReq;
/* // src/pages/ExamProctor/CourseTAReq.tsx
import React, { useState, FocusEvent } from 'react';
import {  useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import SearchSelect from '../../components/SearchSelect';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './CourseTARequest.module.css';

export interface TA {
  id: string;
  name: string;
}

interface RawRequest {
  courseId: string;
  neededTAs: number;
  wantedTAs: string[];
  unwantedTAs: string[];
}

const CourseTARequest: React.FC = () => {
  const { courseID } = useParams<{ courseID: string }>()
  const { courseSec } = useParams<{ courseSec: string }>()
  const courseCode = courseID;
  const courseSection = courseSec;

  const tas: TA[] = [
    { id: 'ta1', name: 'Ali Veli' },
    { id: 'ta2', name: 'Ayşe Fatma' },
    { id: 'ta3', name: 'Mehmet Can' },
  ];

  const initialState = { needed: 0, wanted: [] as string[], unwanted: [] as string[] };
  const [state, setState] = useState(initialState);
  const [resetKey, setResetKey] = useState({ wanted: 0, unwanted: 0 });
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmData, setConfirmData] = useState<RawRequest | null>(null);

  const handleNeededFocus = (e: FocusEvent<HTMLInputElement>) => e.target.select();
  const handleNeededChange = (v: number) => setState(prev => ({ ...prev, needed: v }));

  const handleSelect = (field: 'wanted' | 'unwanted', taId: string) => {
    setState(prev => ({
      needed: prev.needed,
      wanted:
        field === 'wanted'
          ? Array.from(new Set([...prev.wanted, taId]))
          : prev.wanted.filter(id => id !== taId),
      unwanted:
        field === 'unwanted'
          ? Array.from(new Set([...prev.unwanted, taId]))
          : prev.unwanted.filter(id => id !== taId),
    }));
    setResetKey(prev => ({ ...prev, [field]: prev[field] + 1 }));
  };

  const handleDeselect = (field: 'wanted' | 'unwanted', taId: string) =>
    setState(prev => ({
      needed: prev.needed,
      wanted:
        field === 'wanted'
          ? prev.wanted.filter(id => id !== taId)
          : prev.wanted,
      unwanted:
        field === 'unwanted'
          ? prev.unwanted.filter(id => id !== taId)
          : prev.unwanted,
    }));

  const handleSubmit = () => {
    if (state.needed === 0) {
      setErrorMsg("You can't request 0 TAs.");
    } else {
      setConfirmData({
        courseId: courseCode,
        neededTAs: state.needed,
        wantedTAs: state.wanted,
        unwantedTAs: state.unwanted,
      });
    }
  };

  const handleConfirm = () => {
    if (!confirmData) return;
    console.log('Payload:', confirmData);
    // TODO: POST payload to backend

    // reset all inputs on approval
    setConfirmData(null);
    setState(initialState);
    setResetKey({ wanted: 0, unwanted: 0 });
  };

  const selectedSet = new Set([...state.wanted, ...state.unwanted]);

  return (
    <div className={styles.container}>
      
      <div className={styles.headerRow}>
        <BackBut to="/instructor" />
        <h1 className={styles.title}>${courseCode}-${courseSec} TA Request</h1>
      </div>

      <div className={styles.card}>
        <div className={styles.cardHeader}>{courseCode}</div>

        <label className={styles.label}>
          TA Needed:
          <input
            type="number"
            min={0}
            value={state.needed}
            onFocus={handleNeededFocus}
            onChange={e => handleNeededChange(+e.target.value)}
            className={styles.inputNumber}
          />
        </label>

        <div className={styles.controlsRow}>
          <div className={styles.selectorRow}>
            <div className={styles.selectorBlock}>
              <span className={styles.selectorTitle}>Wanted TAs:</span>
              <SearchSelect<TA>
                key={`wanted-${resetKey.wanted}`}
                options={tas.filter(t => !selectedSet.has(t.id))}
                filterOption={t => t.name}
                renderOption={t => <>{t.name}</>}
                placeholder="Pick a TA…"
                onSelect={t => handleSelect('wanted', t.id)}
                className={styles.searchSelect}
              />
              <div className={styles.selectedList}>
                {state.wanted.map(id => {
                  const ta = tas.find(t => t.id === id)!;
                  return (
                    <span key={id} className={styles.tag}>
                      {ta.name}
                      <button
                        type="button"
                        className={styles.tagRemove}
                        onClick={() => handleDeselect('wanted', id)}
                      >
                        ×
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>

            <div className={styles.selectorBlock}>
              <span className={styles.selectorTitle}>Unwanted TAs:</span>
              <SearchSelect<TA>
                key={`unwanted-${resetKey.unwanted}`}
                options={tas.filter(t => !selectedSet.has(t.id))}
                filterOption={t => t.name}
                renderOption={t => <>{t.name}</>}
                placeholder="Pick a TA…"
                onSelect={t => handleSelect('unwanted', t.id)}
                className={styles.searchSelect}
              />
              <div className={styles.selectedList}>
                {state.unwanted.map(id => {
                  const ta = tas.find(t => t.id === id)!;
                  return (
                    <span key={id} className={styles.tag}>
                      {ta.name}
                      <button
                        type="button"
                        className={styles.tagRemove}
                        onClick={() => handleDeselect('unwanted', id)}
                      >
                        ×
                      </button>
                    </span>
                  );
                })}
              </div>
            </div>
          </div>

          <div className={styles.cardSubmitWrapper}>
            <GreenBut text="Submit Request" onClick={handleSubmit} />
          </div>
        </div>
      </div>

      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}

      {confirmData && (
        <ConPop
          message="Are you sure you want to submit this request?"
          onConfirm={handleConfirm}
          onCancel={() => setConfirmData(null)}
        />
      )}
    </div>
  );
};

export default CourseTARequest; */
