/* src/pages/SetWorkload/SetWorkload.tsx */
import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import BackBut from '../../components/Buttons/BackBut';
import GreenBut from '../../components/Buttons/GreenBut';
import ErrPopUp from '../../components/PopUp/ErrPopUp';
import ConPop from '../../components/PopUp/ConPop';
import styles from './SetWorkload.module.css';

interface WorkloadCap {
  fullTimeCap: number;
  partTimeCap: number;
}

// Example initial caps
const exampleCap: WorkloadCap = {
  fullTimeCap: 20,
  partTimeCap: 10,
};

const SetWorkload: React.FC = () => {
  const navigate = useNavigate();

  // state + ref for detecting changes
  const [workloadCap, setWorkloadCap] = useState<WorkloadCap>(exampleCap);
  const initialCap = useRef<WorkloadCap>(exampleCap);

  const [error, setError] = useState<string | null>(null);
  const [saveConfirmOpen, setSaveConfirmOpen] = useState(false);
  const [exitConfirmOpen, setExitConfirmOpen] = useState(false);

  const showError = (msg: string) => setError(msg);

  const handleSave = () => {
    const { fullTimeCap, partTimeCap } = workloadCap;
    if (fullTimeCap < 0 || partTimeCap < 0) {
      showError('Capacities must be zero or positive.');
    } else {
      setSaveConfirmOpen(true);
    }
  };

  const onSaveConfirm = () => {
    // TODO: persist to backend
    // after saving, update the ref to current
    initialCap.current = { ...workloadCap };
    setSaveConfirmOpen(false);
  };

  // Back‐button logic
  const handleBackClick = () => {
    const curr = workloadCap;
    const init = initialCap.current;
    if (
      curr.fullTimeCap !== init.fullTimeCap ||
      curr.partTimeCap !== init.partTimeCap
    ) {
      setExitConfirmOpen(true);
    } else {
      navigate('/department-office');
    }
  };

  const onExitSave = () => {
    // save first, then navigate
    onSaveConfirm();
    setExitConfirmOpen(false);
    navigate('/department-office');
  };

  const onExitDiscard = () => {
    // just navigate, discarding edits
    setExitConfirmOpen(false);
    navigate('/department-office');
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.headerRow}>
        <BackBut onClick={handleBackClick} />
        <h1 className={styles.title}>Set Workload Capacity</h1>
      </div>

      <div className={styles.card}>
        <div className={styles.formContainer}>
          <div className={styles.fieldRow}>
            <label htmlFor="fullTime">Full-Time TA Workload Cap:</label>
            <input
              id="fullTime"
              type="number"
              min={0}
              value={workloadCap.fullTimeCap}
              onChange={e =>
                setWorkloadCap(prev => ({
                  ...prev,
                  fullTimeCap: +e.target.value,
                }))
              }
            />
          </div>
          <div className={styles.fieldRow}>
            <label htmlFor="partTime">Part-Time TA Workload Cap:</label>
            <input
              id="partTime"
              type="number"
              min={0}
              value={workloadCap.partTimeCap}
              onChange={e =>
                setWorkloadCap(prev => ({
                  ...prev,
                  partTimeCap: +e.target.value,
                }))
              }
            />
          </div>
          <div className={styles.buttonsRow}>
            <GreenBut text="Save" onClick={handleSave} />
          </div>
        </div>
      </div>

      {error && <ErrPopUp message={error} onConfirm={() => setError(null)} />}

      {/* Save confirmation */}
      {saveConfirmOpen && (
        <ConPop
          message={`Save capacities? Full-Time: ${workloadCap.fullTimeCap}, Part-Time: ${workloadCap.partTimeCap}`}
          onConfirm={onSaveConfirm}
          onCancel={() => setSaveConfirmOpen(false)}
        />
      )}

      {/* Exit-with-unsaved-changes confirmation */}
      {exitConfirmOpen && (
        <ConPop
          message="You have unsaved changes. Save before leaving?"
          onConfirm={onExitSave}
          onCancel={onExitDiscard}
        />
      )}
    </div>
  );
};

export default SetWorkload;
