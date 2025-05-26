function UploadedFileItem({name, url}) {
    return (
        <div>
            <strong>{name}</strong>: <a href={url} target="_blank" rel="noopener noreferrer">{url}</a>
        </div>
    );
}

export default UploadedFileItem;