import React, { useState } from 'react';
import AddIcon from '@material-ui/icons/Add';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import axios from 'axios';

export default function AddJob() {
    const [showForm, setShowForm] = useState(false);
    const [ip, setIp] = useState('');
    const [jar, setJar] = useState<File>();
    function handleClose() {

        jar?.arrayBuffer().then(buffer => {
            const blob = new Blob([buffer], { type: jar.type });
            let fd = new FormData();
            console.log(jar instanceof Blob);

            fd.append('jarfile', blob, jar.name);

            axios.post(ip + '/jars/upload',
                fd
                , {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            ).then(r => console.log(r));

            setShowForm(() => false)
        }
        );
    }

    return (
        <div>
            <button onClick={() => setShowForm(true)} className="rounded-md text-gray-100 bg-blue-600 flex flex-row w-max p-2 transition duration-200 hover:opacity-80 focus:outline-none opacity-100 ease-in-out">
                <AddIcon />
                Add Job
            </button>
            <hr className="mt-1 border-t-1 border-gray-700" />
            <Dialog open={showForm} onClose={handleClose} aria-labelledby="form-dialog-title">
                <DialogTitle className="text-gray-500">Add new DSP Job</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        To add a new Job input an IP Adress and a jar for the job.
                    </DialogContentText>
                    <form>
                        <TextField
                            autoFocus
                            margin="dense"
                            id="IP"
                            variant="outlined"
                            label="Flink API IP Adress"
                            type="text"
                            required
                            error
                            fullWidth
                            onChange={(e) => setIp(e.target.value)}
                        />
                        <label htmlFor="jar">
                            <input type="file" name="jar" id="jar" onChange={(e) => setJar(e.target.files?.[0])} required />
                        </label>
                    </form>
                </DialogContent>
                <DialogActions className="flex flex-row">
                    <button className="py-1 px-2 border-2 border-red-500 transition duration-200 opacity-100 focus:no-underline hover:opacity-80 rounded-md" onClick={() => setShowForm(false)} color="primary">
                        Cancel
                    </button>
                    <button className="py-1 px-2 border-2 border-green-500 transition duration-200 opacity-100 focus:no-underline hover:opacity-80 rounded-md" onClick={handleClose} color="primary">
                        Subscribe
                    </button>
                </DialogActions>
            </Dialog>
        </div>
    );
}