import React from 'react';
import styles from './NotificationMes.module.css';
import TickBut from '../Buttons/TickBut';
import CrossBut from '../Buttons/CrossBut';
import ConPop from '../PopUp/ConPop';

export interface NotificationMesProps {
  /** The message text */
  label: string;
  /** Show or hide the entire message */
  visible: boolean;
  /** Callback to open the confirm popup */
  onApproveClick: () => void;
  /** Callback for immediate reject */
  onRejectClick: () => void;
  /** Whether the confirmation popup is open */
  confirmOpen: boolean;
  /** Text for the confirm popup */
  confirmText?: string;
  /** Confirm button handler */
  onConfirmApprove: () => void;
  /** Cancel button handler */
  onCancelConfirm: () => void;
  /** Optional style variant */
  variantClass?: string;
}

const NotificationMes: React.FC<NotificationMesProps> = ({
  label,
  visible,
  onApproveClick,
  onRejectClick,
  confirmOpen,
  confirmText = 'Are you sure?',
  onConfirmApprove,
  onCancelConfirm,
  variantClass = ''
}) => {
  if (!visible) return null;

  return (
    <>
      <div className={`${styles.container} ${variantClass}`}> 
        <span className={styles.message}>{label}</span>
        <div className={styles.buttons}>
          <TickBut onApprove={onApproveClick} />
          <CrossBut onReject={onRejectClick} />
        </div>
      </div>

      {confirmOpen && (
        <ConPop
          message={confirmText}
          onConfirm={onConfirmApprove}
          onCancel={onCancelConfirm}
        />
      )}
    </>
  );
};

export default NotificationMes;