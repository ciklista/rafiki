import React, {useEffect, useState} from 'react';
import ReactFlow, {
    ReactFlowProvider,
    addEdge,
    isNode,
    Node,
    Position,
    Controls
} from 'react-flow-renderer';
import dagre from 'dagre';
const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));
const getLayoutedElements = (elements: any, direction: string) => {
    const isHorizontal = direction === 'LR';

    dagreGraph.setGraph({ rankdir: direction });
    elements.forEach((el: any) => {
        if (isNode(el)) {
            dagreGraph.setNode(el.id, { width: 150, height: 50 });
        } else {
            dagreGraph.setEdge(el.source, el.target);
        }
    });
    dagre.layout(dagreGraph);
    return elements.map((el: Node) => {
        if (isNode(el)) {
            const nodeWithPosition = dagreGraph.node(el.id);
            el.targetPosition = isHorizontal ? Position.Left : Position.Top;
            el.sourcePosition = isHorizontal ? Position.Right : Position.Bottom;
            el.position = {
                x: nodeWithPosition.x + Math.random() / 1000,
                y: nodeWithPosition.y,
            };
        }
        return el;
    });
};
function JobGraph(param: any) {
    const [elements, setElements] = useState<any>();
    useEffect(() => {
        setElements(() => getLayoutedElements(param.initialElements, 'LR'))
    }, [param])

    return (
        <div className="h-96">
            <ReactFlowProvider>
                <ReactFlow
                    elements={elements}
                    onLoad={i => i.fitView()}
                >
                    <Controls />
                </ReactFlow>
            </ReactFlowProvider>
        </div>
    );
}

export default JobGraph;