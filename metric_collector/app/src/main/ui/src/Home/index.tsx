import Experiment from '../models/Experiment';
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
            {jobs.map((job: Experiment, index: number) =>
                <Link to={{
                        pathname: `/experiment/${job.jar_id}`,
                        state: job
                    }} className='server transition duration-200 opacity-100 hover:opacity-80' key={index}>
                    <div>{job.name}</div>
                    <div>{job.ip_adress}</div>
                </Link>
            )}
        </div>
    )
}