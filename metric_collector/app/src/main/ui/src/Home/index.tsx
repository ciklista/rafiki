import Job from '../models/Job';
import React from 'react';
import './Home.css';
import { Link } from 'react-router-dom';
import AddExperiment from '../AddExperiment';
import useLocalState from '../useLocalState';

export default function Home() {

    const [servers, _] = useLocalState([], 'job-list');

    return (
        <div className="content">
            <AddExperiment/>
            {servers.map((server: Job, index: number) =>
                <Link to={`/server/${server.ip_adress}`} className='server transition duration-200 opacity-100 hover:opacity-80' key={index}>
                    <div>{server.name}</div>
                    <div>{server.ip_adress}</div>
                </Link>
            )}
        </div>
    )
}