"use strict";

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    const apiBaseURL = "/api/template/";
    let peers = [];

    //$http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    //$http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openCreateProjectModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal-create-project.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });
        modalInstance.result.then(() => {}, () => {});
    };

    //modalInstance is the same variable name as above.
    demoApp.openCloseProjectModal = () => {
            const modalInstance = $uibModal.open({
                templateUrl: 'demoAppModal-close-project.html',
                controller: 'ModalInstanceCtrl-close',
                controllerAs: 'modalInstance',
                resolve: {
                    demoApp: () => demoApp,
                    apiBaseURL: () => apiBaseURL,
                    peers: () => peers
                }
            });
            modalInstance.result.then(() => {}, () => {});
        };

    demoApp.openCreateAgreementModal = () => {
                const modalInstance = $uibModal.open({
                    templateUrl: 'demoAppModal-create-agreement.html',
                    controller: 'ModalInstanceCtrl-create-agreement',
                    controllerAs: 'modalInstance',
                    resolve: {
                        demoApp: () => demoApp,
                        apiBaseURL: () => apiBaseURL,
                        peers: () => peers
                    }
                });
                modalInstance.result.then(() => {}, () => {});
            };


    demoApp.getProjects = () => $http.get(apiBaseURL + "getProjects")
        .then((response) => demoApp.projects = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    /*demoApp.getAgreements = () => $http.get(apiBaseURL + "getAgreements")
        .then((response) => demoApp.agreements = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());*/

    demoApp.getProjects();
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createProjectEndpoint = `${apiBaseURL}createProject?ProjectName=${modalInstance.form.project-name}&ProjectValue=${modalInstance.form.project-value}&EstimatedProjectCost=${modalInstance.form.project-value}&Bank=${modalInstance.form.project-bank}&offtaker=${modalInstance.form.project-offtaker}`;

            $http.put(createProjectEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getProjects();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        modalInstanceTwo.result.then(() => {}, () => {});
    };

    modalInstance.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return false;
    }
});

app.controller('ModalInstanceCtrl-create-agreement', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();
            const createAgreementEndpoint = `${apiBaseURL}GenerateSecurityAgreement?ProjectName=${modalInstance.form.projectnameagreement}`;

            // Create PO and handle success / fail responses.
            $http.put(createAgreementEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    //demoApp.getAgreements();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    modalInstance.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return false;
    }
});

/*app.controller('ModalInstanceCtrl-close', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            //needs to change to a close project endpoint
            // when constant name changes, below needs to change with it.
            const createIOUEndpoint = `${apiBaseURL}create-iou?partyName=${modalInstance.form.counterparty}&iouValue=${modalInstance.form.value}`;

            // Create PO and handle success / fail responses.
            $http.put(createIOUEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getProjects();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return isNaN(modalInstance.form.value) || (modalInstance.form.counterparty === undefined);
    }
});*/

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});