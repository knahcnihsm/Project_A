import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { AdmissionProvider } from './context/AdmissionContext';
import { AppRoutes } from './routes/AppRoutes';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AdmissionProvider>
          <AppRoutes />
        </AdmissionProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
};

export default App;
