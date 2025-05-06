import React, { useRef, ChangeEvent } from 'react';
import styles from './BulkUpload.module.css';
import GreenButton from './Buttons/GreenBut';
import BlueButton from './Buttons/BlueBut';

interface BulkUploadProps {
  label: string;
  onUpload: (file: File) => void;
  primaryButtonText: string;
  onPrimaryClick: () => void;
  secondaryButtonText: string;
  onSecondaryClick: () => void;
}

const BulkUpload: React.FC<BulkUploadProps> = ({
  label,
  onUpload,
  primaryButtonText,
  onPrimaryClick,
  secondaryButtonText,
  onSecondaryClick,
}) => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) onUpload(file);
  };

  const openFileDialog = () => {
    fileInputRef.current?.click();
  };

  return (
    <>
      <div className={styles.bulkUpload}>
        <label className={styles.label}>{label}</label>
        <div className={styles.uploadSection}>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            className={styles.fileInput}
          />
          <span className={styles.chooseText} onClick={openFileDialog}>
            Choose File
          </span>
        </div>
        <div className={styles.buttonGroup}>
          <GreenButton text={primaryButtonText} onClick={onPrimaryClick} />
          <BlueButton text={secondaryButtonText} onClick={onSecondaryClick} />
        </div>
      </div>
    </>
  );
};

export default BulkUpload;