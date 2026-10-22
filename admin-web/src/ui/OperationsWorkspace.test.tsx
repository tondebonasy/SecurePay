import React from "react";
import {
  render,
  screen,
  waitFor,
  cleanup,
  fireEvent,
} from "@testing-library/react";
import { afterEach, describe, it, expect, vi } from "vitest";
import Workspace from "./OperationsWorkspace";
import { api } from "../util/api";
vi.mock("../util/api", () => ({ api: { get: vi.fn(), post: vi.fn() } }));
afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});
const renderApp = () =>
  render(
    <Workspace
      users={<p>Customer management</p>}
      invites={<p>Access invitations</p>}
    />,
  );
const analytics = {
  currency: "USD",
  totalValue: 120,
  totalTx: 3,
  successful: 1,
  pending: 1,
  failed: 1,
  successRate: 33.33,
  daily: { "2026-10-01": 120 },
  statuses: { CREATED: 1 },
  methods: { PAYNOW: 3 },
};
function setup() {
  vi.mocked(api.get).mockImplementation(
    async (url: any) =>
      ({
        data: {
          data: url.includes("analytics")
              ? analytics
              : url.includes("transactions")
                ? { content: [], totalElements: 0 }
                : [],
        },
        headers: {},
      }) as any,
  );
}
describe("SecurePay operations", () => {
  it("renders database metrics", async () => {
    setup();
    renderApp();
    expect(await screen.findByText("33.3%")).toBeTruthy();
    expect(screen.getByText("Transaction value over time")).toBeTruthy();
  });
  it("navigates to paginated transaction empty state", async () => {
    setup();
    renderApp();
    fireEvent.click(screen.getByRole("button", { name: /Transactions/ }));
    expect(
      await screen.findByText("No transactions match these filters."),
    ).toBeTruthy();
    expect(api.get).toHaveBeenCalledWith(
      "/v1/admin/operations/transactions",
      expect.objectContaining({
        params: expect.objectContaining({ page: 0, size: 20, currency: "USD" }),
      }),
    );
  });
  it("prevents incomplete refund submission and explains manual movement", async () => {
    setup();
    renderApp();
    fireEvent.click(screen.getByRole("button", { name: /Refunds/ }));
    expect(
      (
        screen.getByRole("button", {
          name: "Record refund request",
        }) as HTMLButtonElement
      ).disabled,
    ).toBe(true);
    expect(
      screen.getByText(/No funds move through this workflow/),
    ).toBeTruthy();
    fireEvent.change(screen.getByLabelText(/Transaction ID/), {
      target: { value: "tx-1" },
    });
    fireEvent.change(screen.getByLabelText(/Reason \/ investigation/), {
      target: { value: "Customer requested refund" },
    });
    fireEvent.click(
      screen.getByRole("button", { name: "Record refund request" }),
    );
    expect(screen.getByRole("dialog")).toBeTruthy();
    expect(api.post).not.toHaveBeenCalled();
  });
  it("submits verification only after confirmation and displays recorded result", async () => {
    setup();
    vi.mocked(api.post).mockResolvedValue({ data: {} });
    renderApp();
    fireEvent.click(screen.getByRole("button", { name: /Verification/ }));
    fireEvent.change(screen.getByLabelText(/Transaction ID/), {
      target: { value: "tx-1" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Record review" }));
    fireEvent.click(screen.getByRole("button", { name: "Confirm and record" }));
    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        "/v1/admin/operations/transactions/tx-1/verify",
        { reason: "" },
      ),
    );
    expect(await screen.findByText(/Recorded successfully/)).toBeTruthy();
  });
  it("displays loading and API errors", async () => {
    vi.mocked(api.get).mockImplementation(() => new Promise(() => {}));
    renderApp();
    expect(screen.queryByText("Transaction value over time")).toBeNull();
    cleanup();
    vi.mocked(api.get).mockRejectedValue({
      response: { data: { error: { message: "Access denied" } } },
    });
    renderApp();
    expect(await screen.findByText("Access denied")).toBeTruthy();
  });
  it("preserves customer and invite administration", async () => {
    setup();
    renderApp();
    fireEvent.click(screen.getByRole("button", { name: /Customers/ }));
    expect(screen.getByText("Customer management")).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: /Administration/ }));
    expect(screen.getByText("Access invitations")).toBeTruthy();
  });
});

