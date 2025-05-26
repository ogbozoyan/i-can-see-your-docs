import './App.css'
import {uploadAndStream} from "./api/Document.js";
import {useState} from "react";
import UploadedFileItem from "./components/UploadedFileItem.jsx";

function App() {
    const [uploads, setUploads] = useState([]);
    const [uploading, setUploading] = useState(false);

    function handleFileChange(event) {
        const file = event.target.files[0];
        if (!file) return;

        setUploading(true);
        setUploads([]);

        uploadAndStream(file, (chunk) => {
            setUploads(prev => [...prev, chunk]);
        });
    }

    return (
        <div className="App">
            <h2>Upload Document</h2>
            <input type="file" onChange={handleFileChange} disabled={uploading}/>
            <div>
                {uploads.map(({name, url}, index) => (
                    <UploadedFileItem key={index} name={name} url={url}/>
                ))}
            </div>
        </div>
    );
}

export default App
