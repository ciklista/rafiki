import Job from '../models/Job';
import React from 'react';
import './Home.css';
import { Link } from 'react-router-dom';
import AddExperiment from '../AddExperiment';
import useLocalState from '../useLocalState';

export default function Home() {

    const [jobs, _] = useLocalState([], 'job-list');

    return (
        <div className="content">
            <AddExperiment/>
            {jobs.map((job: Job, index: number) =>
                <Link to={`/experiment/${job.jar_id}`} className='server transition duration-200 opacity-100 hover:opacity-80' key={index}>
                    <div>{job.name}</div>
                    <div>{job.ip_adress}</div>
                </Link>
            )}
        </div>
    )
}