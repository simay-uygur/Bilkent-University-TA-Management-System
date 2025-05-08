import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import SearchSelect from '../../components/SearchSelect';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './AssignTATask.module.css';

export interface TA {
  id: string;
  name: string;
}

const sampleTAs: TA[] = [
  { id: 'ta1', name: 'Ali Veli' },
  { id: 'ta2', name: 'Ayşe Fatma' },
  { id: 'ta3', name: 'Mehmet Can' },
  { id: 'ta4', name: 'Elif Yilmaz' },
];

const AssignTATask: React.FC = () => {
  const { courseID } = useParams<{ courseID: string }>()
  const { courseSec } = useParams<{ courseSec: string }>()
  const navigate = useNavigate();

  // pre-existing assigned
  const [alreadyAssigned, setAlreadyAssigned] = useState<TA[]>([
    { id: 'ta2', name: 'Ayşe Fatma' },
  ]);
  // newly selected
  const [selected, setSelected] = useState<TA[]>([]);
  const [resetKey, setResetKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const available = sampleTAs.filter(
    ta =>
      !alreadyAssigned.some(a => a.id === ta.id) &&
      !selected.some(s => s.id === ta.id)
  );

  const handleSelect = (ta: TA) => {
    setSelected(prev => [...prev, ta]);
    setResetKey(k => k + 1);
  };

  const handleRemoveSelected = (ta: TA) => {
    setSelected(prev => prev.filter(s => s.id !== ta.id));
  };

  const handleRemoveAssigned = (ta: TA) => {
    setAlreadyAssigned(prev => prev.filter(a => a.id !== ta.id));
  };

  const handleSubmit = () => {
    if (selected.length === 0 && alreadyAssigned.length === 0) {
      setError('Please have at least one TA assigned.');
    } else {
      setConfirmOpen(true);
    }
  };

  const handleConfirm = () => {
    // merge new into existing
    const merged = [...alreadyAssigned, ...selected];
    console.log('Final assigned:', merged, 'for course', courseID);
    // TODO: send merged to backend
    setConfirmOpen(false);
    navigate(`/instructor/workload/${courseID}/${courseSec}`);
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut to={`/instructor/workload/${courseID}/${courseSec}`} />
        <h1 className={styles.title}>Assign Task To TAs</h1>
      </div>

      <div className={styles.mainContainer}>
        <SearchSelect<TA>
          key={resetKey}
          options={available}
          filterOption={ta => ta.name}
          renderOption={ta => <>{ta.name}</>}
          placeholder="Search TAs…"
          onSelect={handleSelect}
          className={styles.search}
        />

        <div className={styles.assignedContainer}>
          <h2>Assigned TAs</h2>
          <div className={styles.tags}>
            {alreadyAssigned.map(ta => (
              <span key={ta.id} className={styles.tag}>
                {ta.name}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveAssigned(ta)}
                >
                  ×
                </button>
              </span>
            ))}
            {selected.map(ta => (
              <span key={ta.id} className={styles.tag}>
                {ta.name}
                <button
                  type="button"
                  className={styles.tagRemove}
                  onClick={() => handleRemoveSelected(ta)}
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        </div>

        <button className={styles.submitBtn} onClick={handleSubmit}>
          Confirm Assignment
        </button>
      </div>

      {error && <ErrPopUp message={error} onConfirm={() => setError(null)} />}

      {confirmOpen && (
        <ConPop
          message={`Assign ${alreadyAssigned.length + selected.length} TA${alreadyAssigned.length + selected.length > 1 ? 's' : ''}?`}
          onConfirm={handleConfirm}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </div>
  );
};

export default AssignTATask;