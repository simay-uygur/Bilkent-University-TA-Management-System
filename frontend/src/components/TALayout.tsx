import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import TANavBar from '../components/NavBars/TANavBar.tsx';
import NotificationPanel from '../Benim/NotificationPanel.tsx';
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
      <TANavBar/>
      <Outlet />
    </>
  );
};

export default Layout;
