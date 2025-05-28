import './App.css'

import UploadedFileItem from "./components/UploadedFileItem.jsx";
import { Button } from '@mui/material';
import { FileUploadButton } from './components/FileUploadButton/index.jsx';
import { Card } from './components/Card/index.jsx';
import { Route, Routes } from 'react-router-dom';
import { MainPage } from './pages/MainPage/index.jsx';
import { ErrorPage } from './pages/Error/index.jsx';


const App = () => {
    return (
        <Routes>
            <Route path='/' element={<MainPage />}/>
            <Route path='*' element={<ErrorPage />} />
        </Routes>
    )
}

export default App
