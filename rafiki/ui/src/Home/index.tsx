import Experiment from '../models/Experiment';
import React from 'react';
import './Home.css';
import {Link, useHistory} from 'react-router-dom';
import AddExperiment from '../AddExperiment';
import useLocalState from '../useLocalState';
import DeleteIcon from '@material-ui/icons/Delete';

export default function Home() {

    const [jobs, setJobs] = useLocalState([], 'job-list');
    const history = useHistory();

    function deleteJob(e: any, index: number): void {
        e.stopPropagation();
        e.preventDefault();
        setJobs((jobs: any) => {
            return jobs.filter((job: any, i: number) => {
                return i !== index;
            });
        });
    }

    return (
        <div className="content">
            <AddExperiment/>
            {jobs.map((job: Experiment, index: number) =>
                <Link to={{
                        pathname: `/experiment/${job.jar_id}`,
                        state: job
                    }} className='server transition duration-200 opacity-100 hover:opacity-80' key={index}>
                    <div className="flex flex-row flex-1 justify-between items-center">
                        <div>{job.name}</div>
                        <div className="mr-1">{job.ip_adress}</div>
                    </div>
                    <div>
                        <button onClick={(e) => deleteJob(e, index)} className="p-1 bg-red-500 rounded">
                            <DeleteIcon />
                        </button>
                    </div>
                </Link>
            )}
        </div>
    )
}