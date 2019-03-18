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
import { GLSPClientContribution } from "@glsp/theia-integration/lib/browser";
import { FrontendApplicationContribution, OpenHandler, WidgetFactory } from "@theia/core/lib/browser";
import { ContainerModule, interfaces } from "inversify";
import { DiagramConfiguration, DiagramManager, DiagramManagerProvider } from "sprotty-theia/lib";

import { PapyrusDiagramConfiguration } from "./diagram/papyrus-diagram-configuration";
import { PapyrusDiagramManager } from "./diagram/papyrus-diagram-manager";
import { PapyrusGLSPDiagramClient } from "./diagram/papyrus-glsp-diagram-client";
import { PapyrusGLSPClientContribution } from "./language/papyrus-glsp-client-contribution";


export default new ContainerModule((bind: interfaces.Bind, unbind: interfaces.Unbind, isBound: interfaces.IsBound, rebind: interfaces.Rebind) => {
    bind(PapyrusGLSPClientContribution).toSelf().inSingletonScope();
    bind(GLSPClientContribution).toService(PapyrusGLSPClientContribution);

    bind(PapyrusGLSPDiagramClient).toSelf().inSingletonScope();

    bind(DiagramConfiguration).to(PapyrusDiagramConfiguration).inSingletonScope();
    bind(PapyrusDiagramManager).toSelf().inSingletonScope();
    bind(FrontendApplicationContribution).toService(PapyrusDiagramManager);
    bind(OpenHandler).toService(PapyrusDiagramManager);
    bind(WidgetFactory).toService(PapyrusDiagramManager);
    bind(DiagramManagerProvider).toProvider<DiagramManager>((context) => {
        return () => {
            return new Promise<DiagramManager>((resolve) => {
                const diagramManager = context.container.get<PapyrusDiagramManager>(PapyrusDiagramManager);
                resolve(diagramManager);
            });
        };
    });
});

