// src/contexts/AuthContext.tsx
import React, { createContext, useState, useEffect, ReactNode } from 'react';
import {jwtDecode} from 'jwt-decode';

interface AuthContextType {
  userId: string | null;
  role: string | null;
  token: string | null;
}

export const AuthContext = createContext<AuthContextType>({
  userId: null, role: null, token: null
});

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [token, setToken]   = useState<string | null>(localStorage.getItem('jwt'));
  const [userId, setUserId] = useState<string | null>(null);
  const [role, setRole]     = useState<string | null>(null);

  useEffect(() => {
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        setUserId(decoded.sub || decoded.id || null);
        setRole(decoded.role || null);
      } catch {
        setUserId(null);
        setRole(null);
      }
    }
  }, [token]);

  return (
    <AuthContext.Provider value={{ userId, role, token }}>
      {children}
    </AuthContext.Provider>
  );
};
