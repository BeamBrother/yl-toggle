import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import DeleteTipsModal from '.';
import { IntlWrapper } from 'components/utils/wrapper';

it('DeleteTipsModal snapshot', async () => {
  await (async () => {
    const { baseElement, rerender } = render(
      <DeleteTipsModal
        open={true}
        onCancel={vi.fn()}
        onConfirm={vi.fn()}
        content={'test content'}
        title={'test title'}
      />,
      {
        wrapper: IntlWrapper,
      }
    );
    expect(baseElement).toMatchSnapshot();

    rerender(
      <DeleteTipsModal
        open={true}
        onCancel={vi.fn()}
        onConfirm={vi.fn()}
        content={'test content'}
        title={'test title'}
        renderFooter={(nodes) => nodes}
      />
    );
    expect(baseElement).toMatchSnapshot();
  })();
});

it('DeleteTipsModal clcik', async () => {
  await (async () => {
    const mockOnCancel = vi.fn();
    const mockOnConfirm = vi.fn();
    render(
      <DeleteTipsModal
        open={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
        content={'test content'}
        title={'test title'}
      />,
      {
        wrapper: IntlWrapper,
      }
    );

    await userEvent.click(screen.getByText('Confirm'));
    await userEvent.click(screen.getByText('Cancel'));

    expect(mockOnCancel).toBeCalled();
    expect(mockOnConfirm).toBeCalled();
  })();
});
