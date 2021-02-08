import axios from 'axios';
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import IServer from '../models/Server'
import Graph from '../Job-Graph/Job-Graph'

export default function Server() {
    let { id } = useParams<{ id: string }>();

    const [server, setServer] = useState<IServer | any>()
    useEffect(() => {
        axios.get(`http://localhost:3000/servers/${id}`)
            .then(response => {
                setServer(response.data)
            }
            )
            .catch(error => console.log(error))
    }, [id])
    return (
        <div>
            <div>{server.id}</div>
            <Graph />
        </div>
    )
}