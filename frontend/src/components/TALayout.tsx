import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import TANavBar from './TaNavBar.tsx';
import NotificationPanel from './NotificationPanel.tsx';
import { fetchNotifications, Notification, markAllNotificationsRead } from '../api.ts';

const Layout: React.FC = () => {
  const [show, setShow] = useState(false);
  const [notes, setNotes] = useState<Notification[]>([]);

  const toggle = () => {
    setShow(s => !s);
    if (!show) {
      fetchNotifications().then(r => setNotes(r.data));
    }
  };

  const handleMarkAll = () => {
    markAllNotificationsRead().then(() => setNotes([]));
  };

  return (
    <>
      <TANavBar onNotificationsClick={toggle} />
      {show && (
        <NotificationPanel
          notifications={notes}
          onClose={toggle}
          onMarkAllRead={handleMarkAll}
        />
      )}
      <Outlet />
    </>
  );
};

export default Layout;
