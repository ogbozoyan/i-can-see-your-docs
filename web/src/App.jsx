import "./App.css";

import { Route, Routes } from "react-router-dom";
import { MainPage } from "./pages/MainPage/index.jsx";
import { ErrorPage } from "./pages/Error/index.jsx";
import { DownloadPage } from "./pages/Download/index.jsx";
import { DocumentPage } from "./pages/Document/index.jsx";

const App = () => {
  return (
    <Routes>
      <Route path="/" element={<MainPage />} />
      <Route path="/loading" element={<DownloadPage />} />
      <Route path="/document/:id" element={<DocumentPage />} />
      <Route path="*" element={<ErrorPage />} />
    </Routes>
  );
};

export default App;
