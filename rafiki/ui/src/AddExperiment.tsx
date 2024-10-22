// @ts-ignore
import React, { useState } from 'react';
import AddIcon from '@material-ui/icons/Add';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import { uploadJar } from './FlinkAPIService';
import { startExperiment } from './MetricCollectorService';
// @ts-ignore
import useLocalState from './useLocalState';
import Experiment from './models/Experiment';
import Fade from '@material-ui/core/Fade';
import CircularProgress from '@material-ui/core/CircularProgress';
import { useHistory } from 'react-router-dom';

export default function AddExperiment() {
    const [name, setName] = useState<string>('');
    const [ip, setIp] = useState<string>('');
    const [jar, setJar] = useState<File>();
    const [max, setMax] = useState<number>();
    const [operators, setOperators] = useState<string>();

    const [_, setJobs] = useLocalState([], 'job-list');

    const [showForm, setShowForm] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [uploading, setUploading] = useState(false);

    const history = useHistory();
    function handleSubmit(e: any) {
        if (!jar) return;
        e.preventDefault();

        setSubmitting(true);
        setUploading(true);
        uploadJar(jar, ip).then(r => {
            const new_job: Experiment = {
                name: name,
                ip_adress: ip,
                jar_id: getJarId(r.data.filename)
            };
            setJobs((jobs: Experiment[]) => {

                return jobs.concat(new_job);
            });
            setUploading(false);
            startExperiment(getJarId(r.data.filename), max, operators, ip).then(() => {
                setSubmitting(false);
                setUploading(false);
                history.push({
                    pathname: `/experiment/${getJarId(r.data.filename)}`,
                    state: new_job
                });
            });
        });

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
                Add Experiment
            </button>
            <hr className="my-1 border-t-1 border-gray-700" />
            <Dialog open={showForm} onClose={onClose} aria-labelledby="form-dialog-title">
                <DialogTitle className="text-gray-500">Add new DSP Experiment</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        To add a new Experiment input an IP Address and a jar for the job.
                    </DialogContentText>
                    <form className="flex flex-col">
                        <TextField
                            margin="dense"
                            id="name"
                            variant="outlined"
                            label="Experiment Name"
                            type="text"
                            required
                            fullWidth
                            onChange={(e) => setName(e.target.value)}
                        />
                        <TextField
                            margin="dense"
                            id="IP"
                            variant="outlined"
                            label="Flink API IP Address"
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
                            onChange={(e) => setMax(parseInt(e.target.value))}
                        />
                        <TextField
                            id="operator-name"
                            label="Operator Names (Comma separated list)"
                            variant="outlined"
                            margin="dense"
                            type="text"
                            required
                            fullWidth
                            onChange={(e) => setOperators(e.target.value)}
                        />
                        <label className="my-2" htmlFor="jar">
                            <input type="file" name="jar" id="jar" onChange={(e) => setJar(e.target.files?.[0])} required />
                        </label>
                        <Fade in={!submitting}>
                            <input type="submit" className="py-1 px-2 border-2 border-green-500 transition duration-200 opacity-100 focus:no-underline hover:opacity-80 rounded-md w-max" onClick={handleSubmit} color="primary" value="Submit" />
                        </Fade>
                        <Fade in={submitting}>
                            <div className="flex flex-row items-center">
                                <div>
                                    <CircularProgress />
                                </div>
                                { uploading &&
                                    <span className="ml-2">Uploading Jar to Flink Server. This may take a while</span>
                                }
                                {!uploading &&
                                    <span className="ml-2">Running experiments. This may take a while. You will be redirected when the experiments are finished.</span>
                                }
                            </div>
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