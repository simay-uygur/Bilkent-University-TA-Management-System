// src/components/ProtectedRoute.tsx
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';

interface Props {
  requiredRole?: string;
}

export default function ProtectedRoute({ requiredRole }: Props) {
  const location = useLocation();
  const token = localStorage.getItem('jwt');
  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole) {
    let decoded: any;
    try {
      decoded = jwtDecode(token);
    } catch {
      return <Navigate to="/login" replace />;
    }
    if (decoded.role !== requiredRole) {
      return <Navigate to="/login" replace />;
    }
  }

  return <Outlet />;
}
