// src/pages/ExamProctor/CourseTAReq.tsx
import React, { useState, useEffect, FocusEvent } from 'react';
import { useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './CourseTARequest.module.css';
import axios from 'axios';
import SearchSelect from '../../components/SearchSelect';

// TA interface from API
export interface TA {
  id: number;
  name: string;
  surname: string;
  academicLevel: string;
  totalWorkload: number;
  department: string;
  taType: string;
}

// Raw request to backend
interface RawRequest {
  courseId: string;
  neededTAs: number;
  wantedTAs: number[];
  unwantedTAs: number[];
}

// New interface for sent request state
interface SentRequest {
  payload: RawRequest;
  sent: boolean;
}

const CourseTAReq: React.FC = () => {
  const { courseID } = useParams<{ courseID: string }>();
  const { courseSec } = useParams<{ courseSec: string }>();
  const courseCode = courseID!;
  const courseSection = courseSec!;

  // two modes: if sentRequest.sent is true, show current form; if false, show table of TAs
  const [sentRequest] = useState<SentRequest>({
    payload: { courseId: courseCode, neededTAs: 0, wantedTAs: [], unwantedTAs: [] },
    sent: false,
  });

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [tas, setTAs] = useState<TA[]>([]);

  const initialState = { needed: 0, wanted: [] as number[], unwanted: [] as number[] };
  const [state, setState] = useState(initialState);
  const [resetKey, setResetKey] = useState({ wanted: 0, unwanted: 0 });
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [confirmData, setConfirmData] = useState<RawRequest | null>(null);

  useEffect(() => {
    const fetchTAs = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/ta/department/CS');
        setTAs(response.data);
      } catch (err) {
        console.error(err);
        setError('Failed to load TAs.');
      } finally {
        setLoading(false);
      }
    };
    fetchTAs();
  }, []);

  const showError = (msg: string) => setErrorMsg(msg);

  const handleNeededFocus = (e: FocusEvent<HTMLInputElement>) => e.target.select();
  const handleNeededChange = (v: number) => setState(prev => ({ ...prev, needed: v }));

  const handleSelect = (field: 'wanted' | 'unwanted', taId: number) => {
    setState(prev => ({
      needed: prev.needed,
      wanted: field === 'wanted' ? Array.from(new Set([...prev.wanted, taId])) : prev.wanted.filter(id => id !== taId),
      unwanted: field === 'unwanted' ? Array.from(new Set([...prev.unwanted, taId])) : prev.unwanted.filter(id => id !== taId),
    }));
    setResetKey(prev => ({ ...prev, [field]: prev[field] + 1 }));
  };

  const handleDeselect = (field: 'wanted' | 'unwanted', taId: number) =>
    setState(prev => ({
      needed: prev.needed,
      wanted: field === 'wanted' ? prev.wanted.filter(id => id !== taId) : prev.wanted,
      unwanted: field === 'unwanted' ? prev.unwanted.filter(id => id !== taId) : prev.unwanted,
    }));

  const handleSubmit = () => {
    if (state.needed === 0) {
      showError("You can't request 0 TAs.");
    } else {
      setConfirmData({ courseId: courseCode, neededTAs: state.needed, wantedTAs: state.wanted, unwantedTAs: state.unwanted });
    }
  };

  const handleConfirm = async () => {
    if (!confirmData) return;
    try {
      await axios.post('/api/ta-requests', confirmData);
      // on success, update sentRequest.sent - ideally via state but this constant is false
      setConfirmData(null);
      setState(initialState);
      setResetKey({ wanted: 0, unwanted: 0 });
    } catch {
      showError('Failed to submit TA request.');
    }
  };

  const selectedSet = new Set([...state.wanted, ...state.unwanted]);

  // MODE: show workload table when no request sent
if (!sentRequest.sent) {
  return (
    <div className={styles.container}>
      <BackBut to="/instructor" />

      {tas.length === 0 ? (
        <>
          <h1 className={styles.title}>Your TAs are coming soon!</h1>
          <p className={styles.subtitle}>
            No TA data available yet. Please check back later.
          </p>
        </>
      ) : (
        <>
          <h1 className={styles.title}>
            TA Workloads for {courseCode}-{courseSection}
          </h1>
          <div className={styles.tableWrapper}>
            <table className={styles.workloadTable}>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Surname</th>
                  <th>Level</th>
                  <th>Workload</th>
                </tr>
              </thead>
              <tbody>
                {tas.map((ta, idx) => (
                  <tr
                    key={ta.id}
                    className={idx % 2 === 0 ? styles.evenRow : styles.oddRow}
                  >
                    <td>{ta.name}</td>
                    <td>{ta.surname}</td>
                    <td>{ta.academicLevel}</td>
                    <td>{ta.totalWorkload}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

  // default mode: current request form
  if (loading) return <div className={styles.loading}>Loading...</div>;
  if (error) return <div className={styles.error}>{error}</div>;

  return (
    <div className={styles.container}>
      <BackBut to="/instructor" />
      <h1 className={styles.title}>Course TA Request</h1>
      <div className={styles.card}>
        <div className={styles.cardHeader}>{courseCode}-{courseSection}</div>
        <label className={styles.label}>
          TA Needed:
          <input type="number" min={0} value={state.needed} onFocus={handleNeededFocus} onChange={e => handleNeededChange(+e.target.value)} className={styles.inputNumber} />
        </label>
        <div className={styles.controlsRow}>
          <div className={styles.selectorRow}>
            <div className={styles.selectorBlock}>
              <span className={styles.selectorTitle}>Wanted TAs:</span>
              <SearchSelect<TA> key={`wanted-${resetKey.wanted}`} options={tas.filter(t => !selectedSet.has(t.id))} filterOption={t => `${t.name} ${t.surname}`} renderOption={t => <>{t.name} {t.surname}</>} placeholder="Pick a TA…" onSelect={t => handleSelect('wanted', t.id)} className={styles.searchSelect} />
              <div className={styles.selectedList}>{state.wanted.map(id => {
                const ta = tas.find(t => t.id === id)!;
                return <span key={id} className={styles.tag}>{ta.name} {ta.surname}<button type="button" className={styles.tagRemove} onClick={() => handleDeselect('wanted', id)}>×</button></span>;
              })}</div>
            </div>
            <div className={styles.selectorBlock}>
              <span className={styles.selectorTitle}>Unwanted TAs:</span>
              <SearchSelect<TA> key={`unwanted-${resetKey.unwanted}`} options={tas.filter(t => !selectedSet.has(t.id))} filterOption={t => `${t.name} ${t.surname}`} renderOption={t => <>{t.name} {t.surname}</>} placeholder="Pick a TA…" onSelect={t => handleSelect('unwanted', t.id)} className={styles.searchSelect} />
              <div className={styles.selectedList}>{state.unwanted.map(id => {
                const ta = tas.find(t => t.id === id)!;
                return <span key={id} className={styles.tag}>{ta.name} {ta.surname}<button type="button" className={styles.tagRemove} onClick={() => handleDeselect('unwanted', id)}>×</button></span>;
              })}</div>
            </div>
          </div>
          <div className={styles.cardSubmitWrapper}><GreenBut text="Submit Request" onClick={handleSubmit} /></div>
        </div>
      </div>
      {errorMsg && <ErrPopUp message={errorMsg} onConfirm={() => setErrorMsg(null)} />}
      {confirmData && <ConPop message="Are you sure you want to submit this request?" onConfirm={handleConfirm} onCancel={() => setConfirmData(null)} />}
    </div>
  );
};

export default CourseTAReq;