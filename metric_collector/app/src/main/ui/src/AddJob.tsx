import React, { useState } from 'react';
import AddIcon from '@material-ui/icons/Add';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import axios from 'axios';
import useLocalState from './useLocalState';
import Job from './models/Job';
import Fade from '@material-ui/core/Fade';
import CircularProgress from '@material-ui/core/CircularProgress';

export default function AddJob() {
    const [showForm, setShowForm] = useState(false);
    const [ip, setIp] = useState('');
    const [jar, setJar] = useState<File>();
    const [jobs, setJobs] = useLocalState([], 'job-list');
    const [submitting, setSubmitting] = useState(false);
    function handleSubmit(e: any) {
        e.preventDefault();

        jar?.arrayBuffer().then(buffer => {
            setSubmitting(true);
            const blob = new Blob([buffer], { type: jar.type });
            let fd = new FormData();
            fd.append('jarfile', blob, jar.name);
            axios.post(ip + '/jars/upload',
                fd
                , {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            ).then(r => {
                setJobs((jobs: Job[]) => {
                    console.log(jobs);

                    const new_job: Job = {
                        name: 'Test',
                        ip_adress: ip,
                        jar_id: getJarId(r.data.filename)
                    };
                    return jobs.concat(new_job);
                }
                );
                setSubmitting(false);
            });

        }
        );
    }

    function onClose(e: any): void {
        setShowForm(false);
        setSubmitting(false);
    }

    function getJarId(filename: string): string {
        const pathParts: string[] = filename.split('/');
        return pathParts[pathParts.length - 1];
    }

    return (
        <div>
            <button onClick={() => setShowForm(true)} className="rounded-md text-gray-100 bg-blue-600 flex flex-row w-max p-2 transition duration-200 hover:opacity-80 focus:outline-none opacity-100 ease-in-out">
                <AddIcon />
                Add Job
            </button>
            <hr className="my-1 border-t-1 border-gray-700" />
            <Dialog open={showForm} onClose={onClose} aria-labelledby="form-dialog-title">
                <DialogTitle className="text-gray-500">Add new DSP Job</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        To add a new Job input an IP Adress and a jar for the job.
                    </DialogContentText>
                    <form className="flex flex-col">
                        <TextField
                            autoFocus
                            margin="dense"
                            id="IP"
                            variant="outlined"
                            label="Flink API IP Adress"
                            type="text"
                            required
                            fullWidth
                            onChange={(e) => setIp(e.target.value)}
                        />
                        <TextField
                            id="max-parallelism"
                            label="Maximum Parallelism"
                            variant="outlined"
                            margin="dense"
                            type="number"
                            required
                            fullWidth
                        />
                        <TextField
                            id="operator-name"
                            label="Operator Names (Comma separated list)"
                            variant="outlined"
                            margin="dense"
                            type="text"
                            required
                            fullWidth
                        />
                        <label className="my-2" htmlFor="jar">
                            <input type="file" name="jar" id="jar" onChange={(e) => setJar(e.target.files?.[0])} required />
                        </label>
                        <Fade in={!submitting}>
                            <input type="submit" className="py-1 px-2 border-2 border-green-500 transition duration-200 opacity-100 focus:no-underline hover:opacity-80 rounded-md w-max" onClick={handleSubmit} color="primary" value="Submit" />
                        </Fade>
                        <Fade in={submitting}>
                            <CircularProgress />
                        </Fade>
                    </form>
                </DialogContent>
                <DialogActions className="flex flex-row">
                    <button className="py-1 px-2 border-2 border-red-500 transition duration-200 opacity-100 focus:no-underline hover:opacity-80 rounded-md" onClick={onClose} color="primary">
                        Cancel
                    </button>
                </DialogActions>
            </Dialog>
        </div>
    );
}