/********************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
import "../css/gefx.css";

import {
    boundsModule,
    buttonModule,
    commandPaletteModule,
    configureModelElement,
    ConsoleLogger,
    decorationModule,
    defaultGLSPModule,
    defaultModule,
    edgeLayoutModule,
    expandModule,
    exportModule,
    fadeModule,
    glspCommandPaletteModule,
    GLSPGraph,
    glspMouseToolModule,
    glspSelectModule,
    hoverModule,
    layoutCommandsModule,
    LogLevel,
    modelHintsModule,
    modelSourceModule,
    openModule,
    overrideViewerOptions,
    paletteModule,
    PolylineEdgeView,
    requestResponseModule,
    routingModule,
    saveModule,
    SCompartment,
    SCompartmentView,
    SEdge,
    SGraphView,
    SLabel,
    SLabelView,
    SNode,
    SPort,
    toolFeedbackModule,
    TYPES,
    validationModule,
    viewportModule
} from "@glsp/sprotty-client/lib";
import executeCommandModule from "@glsp/sprotty-client/lib/features/execute/di.config";
import { Container, ContainerModule } from "inversify";

import { NodeView } from "./gefx-views";
import { GEFxModelFactory } from "./model-factory";


const GEFxDiagramModule = new ContainerModule((bind, unbind, isBound, rebind) => {
    rebind(TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
    rebind(TYPES.LogLevel).toConstantValue(LogLevel.warn);
    rebind(TYPES.IModelFactory).to(GEFxModelFactory).inSingletonScope();
    const context = { bind, unbind, isBound, rebind };
    configureModelElement(context, 'root', GLSPGraph, SGraphView);
    configureModelElement(context, 'node', SNode, NodeView);
    configureModelElement(context, 'label', SLabel, SLabelView);
    configureModelElement(context, 'port', SPort, NodeView);
    configureModelElement(context, 'comp', SCompartment, SCompartmentView);
    configureModelElement(context, 'edge', SEdge, PolylineEdgeView);
});

export default function createContainer(widgetId: string): Container {
    const container = new Container();

    container.load(decorationModule, validationModule, defaultModule, glspMouseToolModule, defaultGLSPModule, glspSelectModule, boundsModule, viewportModule,
        hoverModule, fadeModule, exportModule, expandModule, openModule, buttonModule, modelSourceModule,
        GEFxDiagramModule, saveModule, executeCommandModule, toolFeedbackModule, modelHintsModule,
        commandPaletteModule, glspCommandPaletteModule, paletteModule, requestResponseModule, routingModule, edgeLayoutModule,
        layoutCommandsModule);

    overrideViewerOptions(container, {
        baseDiv: widgetId,
        hiddenDiv: widgetId + "_hidden"
    });

    return container;
}
